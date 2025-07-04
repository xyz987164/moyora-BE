package com.project.moyora.app.controller;

import com.project.moyora.app.Dto.BoardApplicationDto;
import com.project.moyora.app.Dto.BoardListDto;
import com.project.moyora.app.Dto.TagDto;
import com.project.moyora.app.Dto.UserInterestTagsDto;
import com.project.moyora.app.domain.*;
import com.project.moyora.app.repository.BoardApplicationRepository;
import com.project.moyora.app.repository.BoardRepository;
import com.project.moyora.app.repository.LikeRepository;
import com.project.moyora.app.repository.UserRepository;
import com.project.moyora.app.service.UserService;
import com.project.moyora.global.exception.ErrorCode;
import com.project.moyora.global.exception.SuccessCode;
import com.project.moyora.global.exception.model.ApiResponseTemplete;
import com.project.moyora.global.security.CustomUserDetails;
import com.project.moyora.global.tag.InterestTag;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyPageController {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final BoardApplicationRepository boardApplicationRepository;
    private final UserService userService;

    // 찜 추가
    @PostMapping("/boards/{boardId}/liked/{userId}")
    public ResponseEntity<ApiResponseTemplete<String>> likeBoard(@PathVariable Long userId, @PathVariable Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Optional<Like> existingLike = likeRepository.findByUserAndBoard(user, board);
        if (existingLike.isPresent()) {
            return ApiResponseTemplete.error(ErrorCode.ALREADY_LIKED, null);
        }

        Like like = new Like();
        like.setUser(user);
        like.setBoard(board);
        likeRepository.save(like);

        return ApiResponseTemplete.success(SuccessCode.CREATED, "게시물을 찜했습니다.");
    }

    // 찜 취소
    @DeleteMapping("/boards/{boardId}/liked/{userId}")
    public ResponseEntity<ApiResponseTemplete<String>> removeLike(@PathVariable Long userId, @PathVariable Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Like like = likeRepository.findByUserAndBoard(user, board)
                .orElseThrow(() -> new IllegalArgumentException("찜한 게시물이 아닙니다."));

        likeRepository.delete(like);
        return ApiResponseTemplete.success(SuccessCode.CREATE_POST_SUCCESS, "게시물 찜을 취소했습니다.");
    }

    // 관심 태그 조회
    @Transactional
    @GetMapping("/interest-tags")
    public ResponseEntity<ApiResponseTemplete<UserInterestTagsDto>> getInterestTags(Principal principal) {
        User user = getUserByPrincipal(principal);
        UserInterestTagsDto dto = new UserInterestTagsDto(user.getInterestTags());
        return ApiResponseTemplete.success(SuccessCode.GET_POST_SUCCESS, dto);
    }

    // 관심 태그 수정
    @PutMapping("/interest-tags")
    public ResponseEntity<ApiResponseTemplete<String>> updateInterestTags(
            @RequestBody Set<String> tagNames, Principal principal) {

        Set<InterestTag> tags = tagNames.stream()
                .map(InterestTag::valueOf)
                .collect(Collectors.toSet());

        userService.updateInterestTags(principal, tags);
        return ApiResponseTemplete.success(SuccessCode.UPDATE_POST_SUCCESS, "관심 태그 수정 완료");
    }


    // 참여 중인 모임
    @Transactional
    @GetMapping("/boards/participating")
    public ResponseEntity<ApiResponseTemplete<List<BoardListDto>>> getParticipatingBoards(Principal principal) {
        User user = getUserByPrincipal(principal);

        // ApplicationStatus.LOCKED 상태인 지원 리스트 조회
        List<BoardApplication> lockedApplications = boardApplicationRepository.findByApplicantAndStatus(user, ApplicationStatus.LOCKED);
        List<Board> lockedBoards = lockedApplications.stream()
                .map(BoardApplication::getBoard)
                .collect(Collectors.toList());

        // 본인이 작성한 모임도 참여 중으로 간주
        List<Board> createdBoards = boardRepository.findByWriter(user);

        // 둘을 합쳐서 참여 중 모임 리스트 구성
        List<Board> combined = new ArrayList<>(lockedBoards);
        combined.addAll(createdBoards);

        return ApiResponseTemplete.success(SuccessCode.GET_POST_SUCCESS, toDtoList(combined, user));
    }

    // 신청한 모임
    @Transactional
    @GetMapping("/boards/applying")
    public ResponseEntity<ApiResponseTemplete<List<BoardApplicationDto>>> getApplyingBoards(Principal principal) {
        User user = getUserByPrincipal(principal);

        List<BoardApplication> applyingApplications = boardApplicationRepository.findByApplicantAndStatusIn(
                user, List.of(ApplicationStatus.WAITING, ApplicationStatus.ACCEPTED));

        List<BoardApplicationDto> result = applyingApplications.stream()
                .map(application -> BoardApplicationDto.from(application, user, likeRepository)) // <- likeRepository 주입
                .collect(Collectors.toList());

        return ApiResponseTemplete.success(SuccessCode.GET_POST_SUCCESS, result);
    }




    // 찜한 모임
    @GetMapping("/boards/liked")
    public ResponseEntity<ApiResponseTemplete<List<BoardListDto>>> getLikedBoards(Principal principal) {
        User user = getUserByPrincipal(principal);
        List<Board> boards = likeRepository.findLikedBoardsByUser(user);
        return ApiResponseTemplete.success(SuccessCode.CREATE_POST_SUCCESS, toDtoList(boards, user));
    }

    // 작성한 모임
    @GetMapping("/boards/created")
    public ResponseEntity<ApiResponseTemplete<List<BoardListDto>>> getCreatedBoards(Principal principal) {
        User user = getUserByPrincipal(principal);
        List<Board> boards = boardRepository.findByWriter(user);
        return ApiResponseTemplete.success(SuccessCode.GET_POST_SUCCESS, toDtoList(boards, user));
    }

    private User getUserByPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));
    }

    private List<BoardListDto> toDtoList(List<Board> boards, User user) {
        return boards.stream().map(board -> {
            boolean liked = likeRepository.findByUserAndBoard(user, board).isPresent();

            List<TagDto> tagDtos = board.getTags().stream()
                    .map(tag -> new TagDto(tag.getSection(), tag.name(), tag.getDisplayName()))
                    .collect(Collectors.toList());

            return new BoardListDto(
                    board.getTitle(),
                    board.getStartDate(),
                    board.getEndDate(),
                    board.getMeetType(),
                    board.getMeetDetail(),
                    tagDtos,    // 변환된 TagDto 리스트 전달
                    board.getHowMany(),
                    board.getParticipation(),
                    board.getId(),
                    liked,
                    board.isConfirmed()
            );
        }).collect(Collectors.toList());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();

        DashboardResponse response = DashboardResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .gender(user.getGender())
                .verified(user.getVerified())
                .age(user.getAge()) // 기존 User 엔티티의 getAge() 활용
                .links(List.of(
                        "/api/image/upload/icard",
                        "/api/interest-tag",
                        "/api/boards/liked",
                        "/api/boards/applying",
                        "/api/boards/participating",
                        "/api/boards/created"
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class DashboardResponse {
        private Long userId;
        private String name;
        private GenderType gender;
        private boolean verified;
        private int age;
        private List<String> links;
    }


}
