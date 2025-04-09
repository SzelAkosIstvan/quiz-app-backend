package com.vduzzle.QuizApp.repo;

import com.vduzzle.QuizApp.dbo.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepo extends JpaRepository<File, Long> {
}
