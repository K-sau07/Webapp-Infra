package com.csye6225.webapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "file")
@Getter
@Setter
@NoArgsConstructor
public class FileRecord {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();

    @Column(name = "user_id", nullable = false)
    private String userId;
}
