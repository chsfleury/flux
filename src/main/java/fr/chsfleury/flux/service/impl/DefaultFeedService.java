package fr.chsfleury.flux.service.impl;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import fr.chsfleury.flux.core.Time;
import fr.chsfleury.flux.domain.generated.tables.records.ArticleRecord;
import fr.chsfleury.flux.domain.generated.tables.records.FeedRecord;
import fr.chsfleury.flux.domain.repository.ArticleRepository;
import fr.chsfleury.flux.domain.repository.FeedRepository;
import fr.chsfleury.flux.dto.Article;
import fr.chsfleury.flux.dto.FeedInput;
import fr.chsfleury.flux.dto.Flux;
import fr.chsfleury.flux.service.FeedService;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ratpack.exec.Promise;
import ratpack.exec.util.ParallelBatch;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author Charles Fleury
 * @since 24/06/18.
 */
@Slf4j
public class DefaultFeedService implements FeedService {

    private AsyncHttpClient http;
    private final FeedRepository feedRepository;
    private final ArticleRepository articleRepository;
    private final ScheduledExecutorService scheduler;

    public DefaultFeedService(AsyncHttpClient http, final FeedRepository feedRepository, final ArticleRepository articleRepository) {
        this.http = http;
        this.feedRepository = feedRepository;
        this.articleRepository = articleRepository;
        this.scheduler = newSingleThreadScheduledExecutor();
        this.scheduler.scheduleWithFixedDelay(this::scan, 0, 1, TimeUnit.HOURS);
    }

    public int add(FeedInput input) {

        FeedRecord feedRecord = new FeedRecord()
                .setTitle(input.getTitle())
                .setUrl(input.getUrl())
                .setSelector(input.getSelector())
                .setPrefix(input.getPrefix())
                .setSuffix(input.getSuffix());

        log.info("add feed {} -> {}", input.getTitle(), input.getUrl());
        return feedRepository.insert(feedRecord.setNextScan(Time.timestamp()));

    }

    public int remove(String url) {
        log.info("remove feed {}", url);
        return feedRepository.delete(url);
    }

    @Override
    public Flux convert(SyndFeed syndFeed) {
        Flux flux = new Flux(syndFeed.getTitle(), syndFeed.getDescription());

        for (SyndEntry entry : syndFeed.getEntries()) {
            Article article = new Article(
                    entry.getTitle(),
                    entry.getAuthor(),
                    entry.getContents().get(0).getValue(),
                    entry.getCategories().stream().map(SyndCategory::getName).collect(Collectors.toList())
            );
            flux.getArticles().add(article);
        }

        return flux;
    }

    private void scan() {
        List<FeedRecord> feedsToScan = feedRepository.findReadyToScan();
        log.info("scanning {} feed(s)", feedsToScan.size());
        feedsToScan.parallelStream().map()
        ParallelBatch.of(feedsToScan.stream().map(this::scan).collect(toList())).yield()
        feedsToScan.forEach(this::scan);
    }

    private Promise<List<ArticleRecord>> scan(FeedRecord record) {
        return Promise
                .sync(() -> {
                    log.info("scan {} -> {}", record.getTitle(), record.getUrl());
                    return new URI(record.getUrl());
                })
                .flatMap(http::get)
                .flatMap(response -> {
                    XmlReader reader = new XmlReader(response.getBody().getInputStream());
                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(reader);

                    ParallelBatch<ArticleRecord> batch = ParallelBatch.of(
                            feed.getEntries().stream()
                                    .map(entry -> createArticle(record.getUrl(), entry))
                                    .map(article -> improveContent(article, record.getSelector(), record.getPrefix(), record.getSuffix()))
                                    .collect(toList())
                    );

                    return batch.yield();
                })
                .next(articles -> {
                    int saved = articleRepository.insert(articles);
                    if (saved > 0) {
                        log.info("{} articles saved", saved);
                    }
                });
    }

    private CompletableFuture<ArticleRecord> improveContent(ArticleRecord record, String selector, String prefix, String suffix) {
        CompletableFuture<String> content;
        if (isNullOrEmpty(selector)) {
            content = completedFuture(record.getContent());
        } else {
            content = Promise.sync(() -> new URI(record.getUrl()))
                    .flatMap(http::get)
                    .map(response -> selectHtml(response.getBody().getText(), selector));
        }

        if (!isNullOrEmpty(prefix)) {
            content = content.map(html -> removePrefix(html, prefix));
        }

        if (!isNullOrEmpty(suffix)) {
            content = content.map(html -> removeSuffix(html, suffix));
        }

        return content.map(record::setContent);
    }

    private static String selectHtml(String htmlContent, String selector) {
        Document doc = Jsoup.parse(htmlContent);
        return doc.selectFirst(selector).html();
    }

    private static String removePrefix(String htmlContent, String prefix) {
        int index = htmlContent.lastIndexOf(prefix) + prefix.length();
        return htmlContent.substring(index);
    }

    private static String removeSuffix(String htmlContent, String suffix) {
        int index = htmlContent.indexOf(suffix);
        return htmlContent.substring(0, index);
    }

    private static ArticleRecord createArticle(String fluxUrl, SyndEntry entry) {
        return new ArticleRecord()
                .setFluxUrl(fluxUrl)
                .setTitle(entry.getTitle())
                .setAuthor(entry.getAuthor())
                .setUrl(entry.getLink())
                .setContent(entry.getContents().get(0).getValue())
                .setTags(formatCategories(entry.getCategories()));
    }

    private static String formatCategories(List<SyndCategory> categories) {
        return categories.stream()
                .map(SyndCategory::getName)
                .collect(joining(", "));
    }

    public void close() {
        scheduler.shutdown();
    }

}
