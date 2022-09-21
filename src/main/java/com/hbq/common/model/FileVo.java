package com.hbq.common.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件信息
 *
 * @author hbq
 * @date 2022-03-26 12:47:18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileVo {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "文件路径")
    private String filePath;

    @ApiModelProperty(value = "文件大小")
    private Long fileSize;

    @ApiModelProperty(value = "文件类型")
    private String fileType;
}
