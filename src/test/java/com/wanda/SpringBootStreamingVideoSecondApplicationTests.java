package com.wanda;

import com.wanda.entity.Video;
import com.wanda.repository.VideoRepository;
import com.wanda.service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class SpringBootStreamingVideoSecondApplicationTests {

    @Autowired
    VideoService videoService;

    @Autowired
    VideoRepository videoRepository;

    @Test
    void contextLoads() {

//        Optional<Video> video = this.videoRepository.findById("6efa70bc-52f3-4fb7-8be4-613c6d553605");

//        this.videoService.processVideo(video.get());

        this.videoService.getMasterFile("6efa70bc-52f3-4fb7-8be4-613c6d553605");

    }

}
