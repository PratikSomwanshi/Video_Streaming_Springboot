package com.wanda.controller;


import com.wanda.dto.VideoResourceDTO;
import com.wanda.entity.Video;
import com.wanda.repository.VideoRepository;
import com.wanda.service.VideoService;
import com.wanda.utils.enums.VideoConstants;
import com.wanda.utils.exceptions.CustomException;
import com.wanda.utils.exceptions.enums.ErrorCode;
import com.wanda.utils.exceptions.enums.SuccessCode;
import com.wanda.utils.exceptions.response.SuccessResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class VideoController {

    private VideoService videoService;
    private VideoRepository videoRepository;


    public VideoController(VideoService videoService, VideoRepository videoRepository) {
        this.videoService = videoService;
        this.videoRepository = videoRepository;
    }

    @PostMapping("/video")
    public ResponseEntity<SuccessResponse<Video>> saveVideo(@RequestParam("file") MultipartFile file, String title, String description) {
        Video res = this.videoService.saveVideo(
                file,
                title,
                description
        );

        SuccessResponse<Video> success = new SuccessResponse<>("Successfully saved the video", SuccessCode.FILE_SAVED_SUCCESS, res);

        return new ResponseEntity<>(success, HttpStatus.OK);
    }


    @GetMapping("/video/serve/{videoId}")
    public ResponseEntity<?> serveVideo(
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String range
    ) {
            VideoResourceDTO videoResource = this.videoService.serveVideo(videoId, range);

            String contentType = videoResource.getContentType();
            Path filePath = videoResource.getFilePath();

            byte[] buffer = new byte[(int) videoResource.getContentLength()];

            try (InputStream inputStream = Files.newInputStream(filePath)) {
                inputStream.skip(videoResource.getRangeStart()); // Skip to the start of the range
                int bytesRead = inputStream.read(buffer, 0, (int) videoResource.getContentLength()); // Read only the required chunk
                if (bytesRead < videoResource.getContentLength()) {
                    videoResource.setContentLength(bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new CustomException("failed to load video", HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
            }

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT) // 206 for partial content
                    .contentType(MediaType.parseMediaType(contentType))
                    .headers(videoResource.getHeaders())
                    .contentLength(videoResource.getContentLength())  // Use contentLength
                    .body(new ByteArrayResource(buffer));
        }


    @GetMapping("/video/hls/{videoId}/playlist.m3u8")
    public ResponseEntity<FileSystemResource> playlistVideo(@PathVariable String videoId){
        FileSystemResource masterFile = this.videoService.getMasterFile(videoId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .body(masterFile);
    }

    @GetMapping("/video/hls/{videoId}/{segmentName}")
    public ResponseEntity<Resource> serveHlsSegment(@PathVariable String videoId, @PathVariable String segmentName) {

        String filePath = "videos/" + videoId + "/" + "hls" + "/" + segmentName;

        System.out.println(filePath);

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new CustomException("File not found", HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
        }


            Resource resource = new FileSystemResource(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/MP2T"))
                    .body(resource);

    }
    }



