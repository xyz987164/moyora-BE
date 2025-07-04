package com.project.moyora.app.Dto;

import com.project.moyora.app.domain.ApplicationStatus;
import com.project.moyora.app.domain.Notice;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createdTime;
    private List<CommentDto> comments;

    private List<Participant> participants;

    public static NoticeDto fromEntity(Notice notice) {
        return NoticeDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .writer(notice.getWriter() != null ? notice.getWriter().getName() : null)
                .createdTime(notice.getCreatedTime())
                .comments(
                        notice.getComments().stream()
                                .filter(Objects::nonNull)
                                .distinct()
                                .map(CommentDto::fromEntity)
                                .collect(Collectors.toList())
                )
                .participants(
                        notice.getBoard().getApplications().stream()
                                .filter(Objects::nonNull)
                                .filter(app -> app.getStatus() == ApplicationStatus.LOCKED)
                                .map(Participant::fromEntity)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public NoticeDto(Long id, String title, String content, LocalDateTime createdTime ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdTime = createdTime;
    }
}
