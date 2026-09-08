package _Project.Mita.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalBookInfoResponse(
    BookSummary summary
) {}
