package com.bdxh.classbrand.bean;

import java.util.List;

public class ArticleImage {
    public String url;
    public String uri;
    public int width;
    public int height;
    public List<ImageUrl> url_list;

    public static class ImageUrl{
        public String url;
    }
}
