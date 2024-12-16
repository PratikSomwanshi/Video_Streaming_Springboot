package com.wanda.service;

import com.wanda.dto.VideoResourceDTO;
import com.wanda.entity.Video;
import com.wanda.repository.VideoRepository;
import com.wanda.utils.enums.VideoConstants;
import com.wanda.utils.exceptions.CustomException;
import com.wanda.utils.exceptions.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
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

        processVideo(saveVideo);

        return this.videoRepository.save(saveVideo);
    }


    public VideoResourceDTO serveVideo(String videoId, String range) {

        Optional<Video> existingVideo = this.videoRepository.findById(videoId);

        if(existingVideo.isEmpty()){
            throw new CustomException("video not found", HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
        }

        Video video = existingVideo.get();

        String contentType = video.getContentType();
        String filePath = video.getFilePath();

        Path path = Paths.get(filePath);


        if(!Files.exists(path)){
            throw new CustomException("file not found", HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
        }

        long fileLength = path.toFile().length();

        long rangeStart = 0;
        if (range != null && range.startsWith("bytes=")) {
            range = range.substring("bytes=".length());
            String[] ranges = range.split("-");
            rangeStart = Long.parseLong(ranges[0]);
        }

        long rangeEnd = Math.min(rangeStart + VideoConstants.CHUNK_SIZE - 1, fileLength - 1); // 1 MB chunk
        long contentLength = rangeEnd - rangeStart + 1;

        System.out.println("rangeStart: " + rangeStart);
        System.out.println("rangeEnd: " + rangeEnd);


        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
        headers.setContentLength(contentLength);
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");
        headers.add("X-Content-Type-Options", "nosniff");



        return new VideoResourceDTO(headers, contentType, contentLength, path,  rangeStart, rangeEnd);
    }




    public void processVideo(Video video) {
        String path = video.getFilePath();
        Path filePath = Paths.get(path);

        Path hlsPath = Paths.get(VideoDIR, video.getId(), "hls");

        try {
            if (!Files.exists(hlsPath)) {
                Files.createDirectories(hlsPath);
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FILE_NOT_FOUND);
        }

        String ffmpegCmd = String.format(
                "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%03d.ts\" \"%s/master.m3u8\" ",
                filePath, hlsPath, hlsPath
        );

        String[] command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Windows system
            command = new String[]{"cmd.exe", "/c", ffmpegCmd};
        } else {
            // Unix/Linux/Mac
            command = new String[]{"/bin/bash", "-c", ffmpegCmd};
        }


        try {
            System.out.println(String.join(" ", command));
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("video processing failed!!");
            }
        }catch(IOException e){
            e.printStackTrace();
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FILE_NOT_FOUND);
        }catch(InterruptedException e){
            e.printStackTrace();
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FILE_NOT_FOUND);
        }

    }

    public FileSystemResource getMasterFile(String videoId) {

        Path filePath = Paths.get(VideoDIR, videoId, "hls", "master.m3u8");

        System.out.println("filePath: " + filePath);

        return new FileSystemResource(filePath.toFile());
    }


}
