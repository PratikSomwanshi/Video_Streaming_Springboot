package com.wanda.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;

import java.io.InputStream;
import java.nio.file.Path;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class VideoResourceDTO {

    private HttpHeaders headers;
    private String contentType;
    private long contentLength;
    private Path filePath;
    private Long rangeStart;
    private Long rangeEnd;
}
