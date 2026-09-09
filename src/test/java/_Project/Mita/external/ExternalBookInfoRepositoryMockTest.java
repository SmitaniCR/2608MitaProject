package _Project.Mita.external;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import _Project.Mita.exception.BookInfoFetchException;

@ExtendWith(MockitoExtension.class)
class ExternalBookInfoRepositoryMockTest {//単体テスト 

	@Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ExternalBookInfoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ExternalBookInfoRepository(restClient);
    }

    //正常系

    @Test
    void fetchBookInfo_正常系_書籍情報が取得できる() {

        String isbn = "9784123456789";

        ExternalBookInfoResponse bookInfo =
                new ExternalBookInfoResponse(new BookSummary("9784123456789", "テス本", "テス太", "テス社", "20000524", null));

        doReturn(requestHeadersUriSpec).when(restClient).get();
        
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(java.util.function.Function.class));

        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of(bookInfo));

        Optional<ExternalBookInfoResponse> result =
                repository.fetchBookInfo(isbn);

        assertTrue(result.isPresent());
        assertSame(bookInfo, result.get());

        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(any(java.util.function.Function.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).body(any(ParameterizedTypeReference.class));
    }

    //空レスポンス
     
    @Test
    void fetchBookInfo_空レスポンス_Emptyが返却される() {

        String isbn = "9784123456789";

        doReturn(requestHeadersUriSpec).when(restClient).get();

        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(java.util.function.Function.class));

        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of());

        Optional<ExternalBookInfoResponse> result =
                repository.fetchBookInfo(isbn);

        assertTrue(result.isEmpty());

        verify(responseSpec).body(any(ParameterizedTypeReference.class));
    }

    //nullレスポンス
     
    @Test
    void fetchBookInfo_nullレスポンス_Emptyが返却される() {

        String isbn = "9784123456789";

        doReturn(requestHeadersUriSpec).when(restClient).get();

        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(java.util.function.Function.class));

        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(null);

        Optional<ExternalBookInfoResponse> result =
                repository.fetchBookInfo(isbn);

        assertTrue(result.isEmpty());

        verify(responseSpec).body(any(ParameterizedTypeReference.class));
    }
    
    @Test
    void fetchBookInfo_リストの要素がnull_Emptyが返却される() {
        
    	String isbn = "9784123456789";
    	doReturn(requestHeadersUriSpec).when(restClient).get();

        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(java.util.function.Function.class));

        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);
        
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(Arrays.asList((ExternalBookInfoResponse) null));
        
        Optional<ExternalBookInfoResponse> result =
                repository.fetchBookInfo(isbn);
        
        assertTrue(result.isEmpty());
    }

    //通信例外
    
    @Test
    void fetchBookInfo_通信例外_BookInfoFetchExceptionがスローされる() {
 
        String isbn = "9784123456789";
        RuntimeException cause = new RuntimeException("通信エラー");

        when(restClient.get())
                .thenThrow(cause);

        BookInfoFetchException exception = assertThrows(
                BookInfoFetchException.class,
                () -> repository.fetchBookInfo(isbn)
        );

        assertEquals("通信失敗: ", exception.getMessage());
        assertSame(cause, exception.getCause());

        verify(restClient).get();
    }
}
