package _Project.Mita.external.stub;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import _Project.Mita.external.BookSummary;
import _Project.Mita.external.ExternalBookInfoResponse;

@RestController
public class TestApiController {

	@GetMapping("/get")
    public ResponseEntity<List<ExternalBookInfoResponse>> get(
            @RequestParam String isbn) {

		System.out.println("★DEBUG TestApiController に到達 isbn=" + isbn);//デバッグ用
        ExternalBookInfoResponse response = new ExternalBookInfoResponse(new BookSummary("9784123456789", "テス本", "テス太", "テス社", "20000524", null));//ここNullはダメな気がする

        return ResponseEntity.ok(List.of(response));
    }
}
