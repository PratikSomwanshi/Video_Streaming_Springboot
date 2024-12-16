package com.wanda.service;

import com.wanda.entity.Video;
import com.wanda.repository.VideoRepository;
import com.wanda.utils.exceptions.CustomException;
import com.wanda.utils.exceptions.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class VideoService {

    @Value("${video.directory}")
    private String VideoDIR;

    private VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public Video saveVideo(MultipartFile file, String title, String description)  {

        if (file.isEmpty()) {
            System.out.println("File is empty");
            throw new CustomException("file not found", HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
        }


        Path videoDirPath = Paths.get(VideoDIR);

        try{

            if (!Files.exists(videoDirPath)) Files.createDirectories(videoDirPath);

        }catch (IOException e){
            e.printStackTrace();
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FILE_NOT_FOUND);
        }

        String originalFilename = file.getOriginalFilename();

        String contentType = file.getContentType();





        String newFileName = UUID.randomUUID()+"_"+originalFilename;
        Path filePath = videoDirPath.resolve(newFileName);


        Video saveVideo = Video
                .builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .contentType(contentType)
                .description(description)
                .filePath(filePath.toString())
                .build();



        try {

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        }catch(IOException e){
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FILE_NOT_FOUND);
        }

        return this.videoRepository.save(saveVideo);
    }
}
