package com.vduzzle.QuizApp.Service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    String saveFile(MultipartFile file, String name);
    byte[] loadCorrespondingImage(Long imageID);
}