package com.cat.llmclassifier.service;

import ai.onnxruntime.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;

@Service
public class OnnxTextClassifier {

    private final OrtEnvironment env;
    private final OrtSession session;

    public OnnxTextClassifier() throws Exception {
        this.env = OrtEnvironment.getEnvironment();

        // Load the ONNX model from resources
        File modelFile = new ClassPathResource("models/text_classifier.onnx").getFile();
        Path modelPath = modelFile.toPath();

        this.session = env.createSession(modelPath.toString(), new OrtSession.SessionOptions());
    }

    public int classifyText(String inputText) throws Exception {
        // Wrap the input text as a tensor
        OnnxTensor inputTensor = OnnxTensor.createTensor(env, new String[][]{{inputText}});

        try (OrtSession.Result results = session.run(Collections.singletonMap("input", inputTensor))) {
            OnnxValue resultValue = results.get(0);  // Get the first output
            float[][] output = (float[][]) resultValue.getValue();

            float botProbability = output[0][1];
            float humanProbability = output[0][0];

            return botProbability > humanProbability ? 1 : 0;
        }
    }
}
