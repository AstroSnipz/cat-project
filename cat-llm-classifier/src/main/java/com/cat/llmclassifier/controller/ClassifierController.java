package com.cat.llmclassifier.controller;

import com.cat.llmclassifier.service.OnnxTextClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/classify")
public class ClassifierController {

    private final OnnxTextClassifier onnxClassifier;

    @Autowired
    public ClassifierController(OnnxTextClassifier onnxClassifier) {
        this.onnxClassifier = onnxClassifier;
    }

    @PostMapping
    public String classify(@RequestBody String inputText) throws Exception {
        int result = onnxClassifier.classifyText(inputText);

        if (result == 1) {
            return "bot";
        } else {
            return "human";
        }
    }
}
