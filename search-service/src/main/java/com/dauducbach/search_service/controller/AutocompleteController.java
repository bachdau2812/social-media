package com.dauducbach.search_service.controller;

import com.dauducbach.search_service.service.SearchAutocompleteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor

public class AutocompleteController {
    SearchAutocompleteService searchAutocompleteService;

    @GetMapping("/autocomplete")
    Mono<Set<String>> getSuggest(@RequestParam String query) {
        return searchAutocompleteService.getSuggestionQuery(query);
    }

    @GetMapping("/post-auto")
    Mono<List<String>> getPostAuto(@RequestParam String query) {
        return searchAutocompleteService.getSuggestionPost(query);
    }

    @GetMapping("/profile-auto")
    Mono<List<String>> getProfileAuto(@RequestParam String query) {
        return searchAutocompleteService.getSuggestionProfile(query);
    }
}
