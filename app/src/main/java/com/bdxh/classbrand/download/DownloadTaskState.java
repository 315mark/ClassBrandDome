package com.bdxh.classbrand.download;

/**
 * 下载任务的状态
 */
public enum DownloadTaskState {
    CREATED,
    DOWNLOADING,
    PAUSING,
    PAUSED,
    DONE,
    ERROR,
    DELETING
}
