package run.halo.app.controller.content.model;

import cn.hutool.core.util.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import run.halo.app.model.dto.TagDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Tag Model.
 *
 * @author ryanwang
 * @date 2020-01-11
 */
@Component
public class TagModel {

    private final TagService tagService;

    private final PostService postService;

    private final PostTagService postTagService;

    private final OptionService optionService;

    private final ThemeService themeService;

    public TagModel(TagService tagService, PostService postService, PostTagService postTagService, OptionService optionService, ThemeService themeService) {
        this.tagService = tagService;
        this.postService = postService;
        this.postTagService = postTagService;
        this.optionService = optionService;
        this.themeService = themeService;
    }

    public String list(Model model) {
        model.addAttribute("is_tags", true);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("tags");
    }

    public String listPost(Model model, String slug, Integer page) {
        // Get tag by slug
        final Tag tag = tagService.getBySlugOfNonNull(slug);
        TagDTO tagDTO = tagService.convertTo(tag);

        final Pageable pageable = PageRequest.of(page - 1, optionService.getArchivesPageSize(), Sort.by(DESC, "createTime"));
        Page<Post> postPage = postTagService.pagePostsBy(tag.getId(), PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postService.convertToListVo(postPage);

        // TODO remove this variable
        final int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);

        // Next page and previous page url.
        StringBuilder nextPageFullPath = new StringBuilder();
        StringBuilder prePageFullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()) {
            nextPageFullPath.append(optionService.getBlogBaseUrl())
                .append("/");
            prePageFullPath.append(optionService.getBlogBaseUrl())
                .append("/");
        } else {
            nextPageFullPath.append("/");
            prePageFullPath.append("/");
        }

        nextPageFullPath.append(optionService.getTagsPrefix())
            .append("/")
            .append(tag.getSlug());
        prePageFullPath.append(optionService.getTagsPrefix())
            .append("/")
            .append(tag.getSlug());

        nextPageFullPath.append("/page/")
            .append(posts.getNumber() + 2)
            .append(optionService.getPathSuffix());

        if (posts.getNumber() == 1) {
            prePageFullPath.append(optionService.getPathSuffix());
        } else {
            prePageFullPath.append("/page/")
                .append(posts.getNumber())
                .append(optionService.getPathSuffix());
        }

        model.addAttribute("is_tag", true);
        model.addAttribute("posts", posts);
        model.addAttribute("rainbow", rainbow);
        model.addAttribute("tag", tagDTO);
        model.addAttribute("nextPageFullPath", nextPageFullPath.toString());
        model.addAttribute("prePageFullPath", prePageFullPath.toString());
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("tag");
    }
}
