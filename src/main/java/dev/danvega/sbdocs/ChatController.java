package dev.danvega.sbdocs;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final OpenAiChatModel chatModel;
    private final VectorStore vectorStore;
    @Value("classpath:/prompts/spring-boot-reference.st")
    private Resource sbPromptTemplate;

    @Autowired
    public ChatController(OpenAiChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }
   
    private List<String> findSimilarDocuments(String message) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(3));
        return similarDocuments.stream().map(Document::getContent).toList();
    }

    @GetMapping("/ai/chat")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));


        return chatModel.stream(prompt);
    }

    @GetMapping("/ai/chat/string")
    public Flux<ChatResponse> generateString(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
//        Prompt prompt = new Prompt(new UserMessage(message));
    System.out.println(message);
    PromptTemplate promptTemplate = new PromptTemplate(sbPromptTemplate);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", message);
        promptParameters.put("documents", String.join("\n", findSimilarDocuments(message)));
        Prompt prompt = promptTemplate.create(promptParameters);
       
        return chatModel.stream(prompt);

//        return Flux.just("I'm good, this is a static response from server");
    }
   
    @GetMapping("/ai/chat/string2")
    public String generateString2(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
    System.out.println("question => " + message);
    String result =  chatModel.call(message);
    System.out.println("answer => " + result);
    return result;
    }
}
