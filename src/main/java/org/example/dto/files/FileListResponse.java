package org.example.dto.files;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Data
@RequiredArgsConstructor
public class FileListResponse {
    private List<FileInfo> files;
}

