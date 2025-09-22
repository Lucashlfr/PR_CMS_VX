package com.messdiener.cms.domain.documents;

import lombok.Value;

@Value
public class StorageFileView {
    String fileName;
    String url;
    long size;
}