package com.example.projectboard.service;

import com.example.projectboard.domain.Article;
import com.example.projectboard.domain.UserAccount;
import com.example.projectboard.domain.type.SearchType;
import com.example.projectboard.dto.ArticleDto;
import com.example.projectboard.dto.ArticleWithCommentsDto;
import com.example.projectboard.dto.UserAccountDto;
import com.example.projectboard.repository.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 게시글")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

  @InjectMocks private ArticleService sut;

  @Mock private ArticleRepository articleRepository;

  @DisplayName("검색어 없이 게시글을 검색하면, 게시글 페이지를 반환한다.")
  @Test
  void givenNoSearchParameters_whenSearchingArticles_thenReturnsArticlePage() {
    // given
    Pageable pageable = Pageable.ofSize(20);
    given(articleRepository.findAll(pageable)).willReturn(Page.empty());

    // when
    Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);
    // 페이지네이션 포함

    // then
    assertThat(articles).isEmpty();
    then(articleRepository).should().findAll(pageable);
  }

  @DisplayName("검색어와 함께 게시글을 검색하면, 게시글 페이지를 반환한다.")
  @Test
  void givenSearchParameters_whenSearchingArticles_thenReturnsArticlePage() {
    // Given
    SearchType searchType = SearchType.TITLE;
    String searchKeyword = "title";
    Pageable pageable = Pageable.ofSize(20);
    given(articleRepository.findByTitleContaining(searchKeyword, pageable)).willReturn(Page.empty());

    // When
    Page<ArticleDto> articles = sut.searchArticles(searchType, searchKeyword, pageable);

    // Then
    assertThat(articles).isEmpty();
    then(articleRepository).should().findByTitleContaining(searchKeyword, pageable);
  }

  @DisplayName("게시글을 조회하면, 게시글을 반환")
  @Test
  void givenArticleId_whenSearchingArticle_thenReturnArticle(){
    // Given
    Long articleId = 1L;
    Article article = createArticle();
    given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

    // When
    ArticleWithCommentsDto dto = sut.getArticle(articleId);

    // Then
    assertThat(dto)
            .hasFieldOrPropertyWithValue("title", article.getTitle())
            .hasFieldOrPropertyWithValue("content", article.getContent())
            .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
    then(articleRepository).should().findById(articleId);
  }

  @DisplayName("없는 게시글을 조회하면, 예외를 던진다.")
  @Test
  void givenNonexistentArticleId_whenSearchingArticle_thenThrowsException() {
    // Given
    Long articleId = 0L;
    given(articleRepository.findById(articleId)).willReturn(Optional.empty());

    // When
    Throwable t = catchThrowable(() -> sut.getArticle(articleId));

    // Then
    assertThat(t)
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("게시글이 없습니다 - articleId: " + articleId);
    then(articleRepository).should().findById(articleId);
  }

  @DisplayName("게시글 정보를 입력하면, 게시글을 생성한다.")
  @Test
  void givenArticleInfo_whenSavingArticle_thenSaveArticle(){
    // given
    ArticleDto dto = createArticleDto();
    given(articleRepository.save(any(Article.class))).willReturn(createArticle());

    // when
    sut.saveArticle(dto);

    // then
    then(articleRepository).should().save(any(Article.class));

  }

  @DisplayName("게시글의 id와 수정 정보를 입력하면, 게시글을 수정한다.")
  @Test
  void givenModifiedArticleInfo_whenUpdatingArticle_thenUpdatesArticle(){
    // given
    Article article = createArticle();
    ArticleDto dto = createArticleDto("새 타이틀", "새 내용", "#springboot");
    given(articleRepository.getReferenceById(dto.id())).willReturn(article);

    // when
    sut.updateArticle(dto);

    // then
    assertThat(article)
            .hasFieldOrPropertyWithValue("title", dto.title())
            .hasFieldOrPropertyWithValue("content", dto.content())
            .hasFieldOrPropertyWithValue("hashtag", dto.hashtag());
    then(articleRepository).should().getReferenceById(dto.id());
  }

  @DisplayName("없는 게시글의 수정 정보를 입력하면, 경고 로그를 찍고 아무 것도 하지 않는다.")
  @Test
  void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing() {
    // Given
    ArticleDto dto = createArticleDto("새 타이틀", "새 내용", "#springboot");
    given(articleRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

    // When
    sut.updateArticle(dto);

    // Then
    then(articleRepository).should().getReferenceById(dto.id());
  }

  @DisplayName("게시글의 id를 입력하면, 게시글을 삭제한다.")
  @Test
  void givenArticleId_whenDeletingArticle_thenDeleteArticle(){
    // given
    Long articleId = 1L;
    willDoNothing().given(articleRepository).deleteById(articleId);

    // when
    sut.deleteArticle(1L);

    // then
    then(articleRepository).should().deleteById(articleId);
  }

  @DisplayName("게시글 수를 조회하면, 게시글 수를 반환한다")
  @Test
  void givenNothing_whenCountingArticles_thenReturnsArticleCount(){
    // given
    long expected = 0L;
    given(articleRepository.count()).willReturn(expected);

    // when
    long actual = sut.getArticleCount();

    // then
    assertThat(actual).isEqualTo(expected);
    then(articleRepository).should().count();
  }


  private UserAccount createUserAccount() {
    return UserAccount.of(
            "minji",
            "password",
            "minji@email.com",
            "Minji",
            null
    );
  }

  private Article createArticle() {
    return Article.of(
            createUserAccount(),
            "title",
            "content",
            "#java"
    );
  }

  private ArticleDto createArticleDto() {
    return createArticleDto("title", "content", "#java");
  }

  private ArticleDto createArticleDto(String title, String content, String hashtag) {
    return ArticleDto.of(1L,
            createUserAccountDto(),
            title,
            content,
            hashtag,
            LocalDateTime.now(),
            "Uno",
            LocalDateTime.now(),
            "Uno");
  }

  private UserAccountDto createUserAccountDto() {
    return UserAccountDto.of(
            1L,
            "minji",
            "password",
            "minji@mail.com",
            "Minji",
            "This is memo",
            LocalDateTime.now(),
            "minji",
            LocalDateTime.now(),
            "minji"
    );
  }

}