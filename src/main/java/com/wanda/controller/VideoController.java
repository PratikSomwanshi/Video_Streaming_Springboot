package com.wanda.controller;


import com.wanda.entity.Video;
import com.wanda.service.VideoService;
import com.wanda.utils.exceptions.enums.SuccessCode;
import com.wanda.utils.exceptions.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class VideoController {

    private VideoService videoService;


    public VideoController(VideoService videoService) {
        this.videoService = videoService;
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


}


