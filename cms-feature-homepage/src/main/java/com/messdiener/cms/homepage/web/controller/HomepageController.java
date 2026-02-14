package com.messdiener.cms.homepage.web.controller;

import com.messdiener.cms.events.domain.entity.Event;
import com.messdiener.cms.shared.enums.ArticleState;
import com.messdiener.cms.shared.enums.ArticleType;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.web.common.security.SecurityHelper;

import com.messdiener.cms.domain.document.ArticleQueryPort;
import com.messdiener.cms.domain.document.ArticleCommandPort;
import com.messdiener.cms.domain.document.ArticleView;

import com.messdiener.cms.events.app.service.EventApplicationService;
import com.messdiener.cms.events.app.service.EventService;
import com.messdiener.cms.events.app.service.PlannerTaskService;
import com.messdiener.cms.events.domain.entity.PlanerTask;

import com.messdiener.cms.liturgy.app.helper.LiturgieHelper;
import com.messdiener.cms.liturgy.persistence.service.LiturgieService;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class HomepageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomepageController.class);

    private final SecurityHelper securityHelper;

    // NEU: Ports statt direktem ArticleService
    private final ArticleQueryPort articleQueryPort;
    private final ArticleCommandPort articleCommandPort;

    // Events bleiben (Feature-Abhängigkeit ist okay; Zyklus bricht bereits durch Domain-Ports auf Documents-Seite)
    private final PlannerTaskService plannerTaskService;
    private final EventService eventService;
    private final EventApplicationService eventApplicationService;

    private final LiturgieService liturgieService;
    private final LiturgieHelper liturgieHelper;

    @PostConstruct
    public void init() {
        LOGGER.info("HomepageController initialized (cms-feature-homepage).");
    }

    @GetMapping("/")
    public String index(Model model) throws SQLException {
        model.addAttribute("articles", articleQueryPort.getArticlesByType(ArticleType.BLOG, ArticleState.PUBLISHED));
        model.addAttribute("events", eventService.getEventsForState());
        return "public/publicIndex";
    }

    @GetMapping("/infos")
    public String infos(Model model) throws SQLException {
        model.addAttribute("events", eventService.getEventsAtDeadline());
        model.addAttribute("now", System.currentTimeMillis());
        return "public/pages/event_overview";
    }

    @GetMapping("/about")
    public String about(Model model) throws SQLException {
        model.addAttribute("article", articleQueryPort.getArticleByType(ArticleType.ABOUT).orElse(ArticleView.empty()));
        return "public/pages/article";
    }

    @GetMapping("/contact")
    public String contact(Model model) throws SQLException {
        model.addAttribute("article", articleQueryPort.getArticleByType(ArticleType.CONTACT).orElse(ArticleView.empty()));
        return "public/pages/article";
    }

    @GetMapping("/impressum")
    public String impressum(Model model) throws SQLException {
        model.addAttribute("article", articleQueryPort.getArticleByType(ArticleType.IMPRESSUM).orElse(ArticleView.empty()));
        return "public/pages/article";
    }

    @GetMapping("/go/{slug}")
    public String go(@PathVariable("slug") String slugInput, Model model, @RequestParam("state") Optional<String> state) {

        String slug = slugInput.toLowerCase();

        Event event = eventService.getArticleBySlug(slug).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        model.addAttribute("event", event);
        model.addAttribute("components", eventApplicationService.getComponents(event.getEventId()));


        model.addAttribute("state", state.orElse("null"));
        return "public/pages/event";
    }


    @GetMapping("/go")
    public String go(Model model, @RequestParam("id") UUID id, @RequestParam(value = "type", required = false) Optional<String> t, @RequestParam(value = "state", required = false) Optional<String> state) throws SQLException {

        model.addAttribute("article", articleQueryPort.getArticleById(id).orElse(ArticleView.empty()));
        model.addAttribute("state", state.orElse("null"));

        final String type = t.orElse("event");
        if ("event".equalsIgnoreCase(type)) {
            model.addAttribute("event", eventService.getEventById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found")));
            model.addAttribute("components", eventApplicationService.getComponents(id));
            return "public/pages/event";
        }

        return "public/pages/article";
    }

    @GetMapping("/homepage")
    public String homepage(HttpSession httpSession, Model model, @RequestParam(value = "article", required = false) Optional<String> idS) throws SQLException {
        securityHelper.addPersonToSession(httpSession);
        model.addAttribute("articles", articleQueryPort.getArticles());

        if (idS.isPresent()) {
            final UUID id = UUID.fromString(idS.get());
            model.addAttribute("article", articleQueryPort.getArticleById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found")));
            model.addAttribute("states", ArticleState.values());
            return "homepage/interface/articleInterface";
        }

        return "homepage/list/articleList";
    }

    @PostMapping("/homepage/create")
    public String createHomepage(@RequestParam("title") String title) throws SQLException {
        final var user = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        final ArticleView article = new ArticleView(UUID.randomUUID(), 0, user.id(), CMSDate.current(), "", ArticleState.CREATED, ArticleType.BLOG, title, "", "", "", "");
        articleCommandPort.saveArticle(article);
        return "redirect:/homepage?article=" + article.id();
    }

    @PostMapping("/article/save")
    public String save(@RequestParam("id") UUID id, @RequestParam("title") String title, @RequestParam("description") String description, @RequestParam(value = "imgUrl", required = false) Optional<String> imgUrl, @RequestParam("form") String form, @RequestParam("html") String html, @RequestParam("state") String state, @RequestParam(value = "task", required = false) Optional<String> task, @RequestParam(value = "event", required = false) Optional<String> event) throws SQLException {

        final ArticleState articleState = ArticleState.valueOf(state.toUpperCase(Locale.ROOT));

        final ArticleView existing = articleQueryPort.getArticleById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));

        final ArticleView updated = new ArticleView(existing.id(), existing.tag(), existing.creator(), CMSDate.current(), existing.target(), articleState, existing.articleType(), title, description, imgUrl.orElse(""), form, html);

        articleCommandPort.saveArticle(updated);

        if (task.isPresent() && updated.articleState() == ArticleState.PUBLISHED) {
            final UUID taskId = UUID.fromString(task.get());
            final UUID eventId = UUID.fromString(event.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event id required to update task")));
            final PlanerTask planerTask = plannerTaskService.getTaskById(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
            planerTask.setState(CMSState.COMPLETED);
            plannerTaskService.updateTask(eventId, planerTask);
        }

        return "redirect:/homepage?article=" + updated.id();
    }

    @GetMapping("/update")
    public String update(Model model) throws SQLException {
        final int year = Year.now().getValue();
        final int week = getCurrentWeekNumberWithSundayShift();

        model.addAttribute("kw", week);
        model.addAttribute("img", "/dist/assets/img/KW/KW" + week + ".png");

        final long[] timestamps = getTimestampsForWeek(year, week);
        model.addAttribute("liturgie", liturgieService.getLiturgies(Tenant.MSGK, timestamps[0], timestamps[1]));
        model.addAttribute("helper", liturgieHelper);

        return "public/pages/update";
    }

    public static long[] getTimestampsForWeek(int year, int weekNumber) {
        final WeekFields wf = WeekFields.of(Locale.GERMANY);
        final LocalDate monday = LocalDate.of(year, 1, 1).with(wf.weekOfWeekBasedYear(), weekNumber).with(wf.dayOfWeek(), 1);
        final LocalDate mondayNextWeek = monday.plusDays(7);
        final ZonedDateTime startOfMonday = monday.atStartOfDay(ZoneId.systemDefault());
        final ZonedDateTime endOfSunday = mondayNextWeek.atStartOfDay(ZoneId.systemDefault()).minusNanos(1);
        return new long[]{startOfMonday.toInstant().toEpochMilli(), endOfSunday.toInstant().toEpochMilli()};
    }

    public static int getCurrentWeekNumberWithSundayShift() {
        LocalDate today = LocalDate.now();
        if (today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            today = today.plusDays(1);
        }
        return today.get(WeekFields.of(Locale.GERMANY).weekOfWeekBasedYear());
    }
}
