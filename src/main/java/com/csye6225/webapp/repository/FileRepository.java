package com.csye6225.webapp.repository;

import com.csye6225.webapp.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileRecord, String> {
}
