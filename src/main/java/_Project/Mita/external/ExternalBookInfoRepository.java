package _Project.Mita.external;

import java.util.List;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import _Project.Mita.exception.BookInfoFetchException;

@Repository
public class ExternalBookInfoRepository {//外部情報をDBに見立て、情報のやり取りをする

    private final RestClient restClient;

    public ExternalBookInfoRepository(RestClient externalBookRestClient) {
        this.restClient = externalBookRestClient;
    }
    
    ParameterizedTypeReference<List<ExternalBookInfoResponse>> responseType = 
    		new ParameterizedTypeReference<List<ExternalBookInfoResponse>>() {};
    
    /**
     * ISBNを条件に外部APIから書籍情報を取得
     *
     * @param isbn 書籍のISBNコード
     * @return 書籍情報
     */
    public Optional<ExternalBookInfoResponse> fetchBookInfo(String isbn) {
        try {
            List<ExternalBookInfoResponse> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/get").queryParam("isbn", isbn).build())
                    .retrieve()
                    .body(responseType);               
            System.out.println("★DEBUG response = " + response);//デバッグ用
            if (response == null || response.isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.get(0));

        } catch (Exception e) {
        	throw new BookInfoFetchException("通信失敗: ", e);
        }
    }
}
