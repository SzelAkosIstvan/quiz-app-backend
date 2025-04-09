package com.vduzzle.QuizApp.Service;

import com.vduzzle.QuizApp.dbo.File;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    File saveFile(MultipartFile file, String name);
    File loadCorrespondingImage(Long imageID);
}