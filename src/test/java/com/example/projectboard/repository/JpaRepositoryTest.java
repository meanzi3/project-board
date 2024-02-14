package com.example.projectboard.repository;

import com.example.projectboard.config.JpaConfig;
import com.example.projectboard.domain.Article;
import com.example.projectboard.domain.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA 연결 테스트")
@Import(JpaConfig.class)
@DataJpaTest
class JpaRepositoryTest {

  private final ArticleRepository articleRepository;
  private final ArticleCommentRepository articleCommentRepository;
  private final UserAccountRepository userAccountRepository;

  JpaRepositoryTest(
          @Autowired ArticleRepository articleRepository,
          @Autowired ArticleCommentRepository articleCommentRepository,
          @Autowired UserAccountRepository userAccountRepository) {
    this.articleRepository = articleRepository;
    this.articleCommentRepository = articleCommentRepository;
    this.userAccountRepository = userAccountRepository;
  }

  @DisplayName("select 테스트")
  @Test
  void givenTestData_whenSelecting_thenWorksFine(){
    // given


    // when
    List<Article> articles = articleRepository.findAll();

    // then
    assertThat(articles)
            .isNotNull()
            .hasSize(123);

  }

  @DisplayName("insert 테스트")
  @Test
  void givenTestData_whenInserting_thenWorksFine(){
    // given
    long previousCount = articleRepository.count();
    UserAccount userAccount = userAccountRepository.save(UserAccount.of("minji","pw", null, null, null));
    Article article = Article.of(userAccount, "new article", "new content", "new hashtag");

    // when
    articleRepository.save(article);

    // then
    assertThat(articleRepository.count()).isEqualTo(previousCount + 1);

  }

  @DisplayName("update 테스트")
  @Test
  void givenTestData_whenUpdating_thenWorksFine(){
    // given
    Article article = articleRepository.findById(1L).orElseThrow();
    String updatedHashTag = "#springboot";
    article.setHashtag(updatedHashTag);

    // when
    Article savedArticle = articleRepository.saveAndFlush(article);

    // then
    assertThat(savedArticle).hasFieldOrPropertyWithValue("hashtag", updatedHashTag);

  }

  @DisplayName("delete 테스트")
  @Test
  void givenTestData_whenDeleting_thenWorksFine(){
    // given
    Article article = articleRepository.findById(1L).orElseThrow();
    long previousArticleCount = articleRepository.count();
    long previousArticleCommentCount = articleCommentRepository.count();
    int deleteCommentsSize = article.getArticleComments().size();

    // when
    articleRepository.delete(article);

    // then
    assertThat(articleRepository.count()).isEqualTo(previousArticleCount - 1);
    assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - deleteCommentsSize);
  }
}
