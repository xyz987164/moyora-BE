package com.project.moyora.app.Dto;

import com.project.moyora.app.domain.ReportStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportStatusUpdateRequest {
    private ReportStatus status;
}