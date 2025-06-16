package com.messdiener.cms.v3.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.v3.app.entities.acticle.Article;
import com.messdiener.cms.v3.app.entities.event.PlanerTask;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.article.ArticleService;
import com.messdiener.cms.v3.app.services.event.EventApplicationService;
import com.messdiener.cms.v3.app.services.event.EventService;
import com.messdiener.cms.v3.app.services.event.PlannerTaskService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.ArticleState;
import com.messdiener.cms.v3.shared.enums.ArticleType;
import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class HomepageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomepageController.class);
    private final Cache cache;
    private final SecurityHelper securityHelper;
    private final ArticleService articleService;
    private final PlannerTaskService plannerTaskService;
    private final EventService eventService;
    private final EventApplicationService eventApplicationService;

    @PostConstruct
    public void init() {
        LOGGER.info("DefaultController initialized.");
    }

    @GetMapping("/")
    public String index(HttpSession httpSession, Model model) throws SQLException {
        model.addAttribute("articles", articleService.getArticlesByType(ArticleType.BLOG, ArticleState.PUBLISHED));
        model.addAttribute("events", eventService.getEvents());
        return "public/index";
    }

    @GetMapping("/infos")
    public String infos(HttpSession httpSession, Model model) throws SQLException {
        model.addAttribute("events", eventService.getEventsAtDeadline());
        model.addAttribute("now", System.currentTimeMillis());
        return "public/pages/event_overview";
    }

    @GetMapping("/about")
    public String about(HttpSession httpSession, Model model)throws SQLException {
        model.addAttribute("article", articleService.getArticleByType(ArticleType.ABOUT).orElse(Article.empty()));
        return "public/pages/article";
    }

    @GetMapping("/contact")
    public String contact(HttpSession httpSession, Model model)throws SQLException {
        model.addAttribute("article", articleService.getArticleByType(ArticleType.CONTACT).orElse(Article.empty()));
        return "public/pages/article";
    }

    @GetMapping("/impressum")
    public String impressum(HttpSession httpSession, Model model)throws SQLException {
        model.addAttribute("article", articleService.getArticleByType(ArticleType.IMPRESSUM).orElse(Article.empty()));
        return "public/pages/article";
    }


    @GetMapping("/go")
    public String go(HttpSession httpSession, Model model, @RequestParam("id")UUID id, @RequestParam("type")Optional<String> t, @RequestParam("state")Optional<String> state) throws SQLException {
        model.addAttribute("article", articleService.getArticleById(id).orElse(Article.empty()));
        model.addAttribute("state", state.orElse("null"));

        String type = t.orElse("event");
        if(type.equals("event")) {
            model.addAttribute("event", eventService.getEventById(id).orElseThrow());
            model.addAttribute("components", eventApplicationService.getComponents(id));
            return "public/pages/event";
        }

        return "public/pages/article";
    }

    @GetMapping("/homepage")
    public String homepage(HttpSession httpSession, Model model, @RequestParam("article") Optional<String> idS) throws SQLException {
        securityHelper.addPersonToSession(httpSession);
        model.addAttribute("articles", articleService.getArticles());

        if(idS.isPresent()) {

            UUID id = UUID.fromString(idS.get());
            model.addAttribute("article", articleService.getArticleById(id).orElseThrow());
            model.addAttribute("states", ArticleState.values());

            return "homepage/interface/articleInterface";
        }

        return "homepage/list/articleList";
    }

    @PostMapping("/homepage/create")
    public RedirectView createHomepage(@RequestParam("title")String title) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Article article = new Article(UUID.randomUUID(), 0, user.getId(), CMSDate.current(),"", ArticleState.CREATED, ArticleType.BLOG,title,  "", "", "", "");
        articleService.saveArticle(article);

        return new RedirectView("/homepage?article=" + article.getId());
    }

    @PostMapping("/article/save")
    public RedirectView save(@RequestParam("id")UUID id, @RequestParam("title")String title, @RequestParam("description")String description,
                             @RequestParam("imgUrl")Optional<String> imgUrl, @RequestParam("form")String form, @RequestParam("html")String html,
                             @RequestParam("state")String state, @RequestParam("task")Optional<String> task, @RequestParam("event")Optional<String> event) throws SQLException {

        ArticleState articleState = ArticleState.valueOf(state.toUpperCase());

        Article article = articleService.getArticleById(id).orElseThrow();
        article.setTitle(title);
        article.setDescription(description);
        article.setImgUrl(imgUrl.orElse(""));
        article.setForm(form);
        article.setHtml(html);
        article.setLastUpdate(CMSDate.current());
        article.setArticleState(articleState);
        articleService.saveArticle(article);

        if(task.isPresent() && article.getArticleState() == ArticleState.PUBLISHED) {
            PlanerTask planerTask = plannerTaskService.getTaskById(UUID.fromString(task.get())).orElseThrow();
            planerTask.setState(CMSState.COMPLETED);
            plannerTaskService.updateTask(UUID.fromString(event.orElseThrow()), planerTask);
        }

        return new RedirectView("/homepage?article=" + article.getId());
    }

}