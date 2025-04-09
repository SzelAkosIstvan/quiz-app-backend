package com.vduzzle.QuizApp.controller;

import com.vduzzle.QuizApp.Service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Object> saveFile(@RequestParam(required = false) MultipartFile file,
                                           @RequestParam(required = false) String fileName) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File and Name are required");
        }
        return ResponseEntity.ok(fileService.saveFile(file, fileName));
    }

    @GetMapping("/load")
    public ResponseEntity<Object> loadFile(@RequestParam(required = false) Long imageID) {
        if (imageID == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image ID is required");
        }
        return ResponseEntity.ok(fileService.loadCorrespondingImage(imageID));
    }
}