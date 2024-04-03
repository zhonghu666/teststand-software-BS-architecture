package com.cetiti.config;

import com.cetiti.entity.FunctionMetadata;
import com.cetiti.expression.array.*;
import com.cetiti.expression.numeric.*;
import com.cetiti.expression.string.*;
import com.cetiti.expression.time.DateFunction;
import com.cetiti.expression.time.SecondsFunction;
import com.cetiti.expression.time.TimeFunction;
import com.cetiti.service.impl.CacheService;
import com.cetiti.utils.RedisUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.lexer.token.OperatorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StartupDataLoader implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Resource
    private CacheService cacheService;

    @Override
    public void run(String... args) throws Exception {
        // 从MongoDB获取数据
        List<FunctionMetadata> functionMetadataList = mongoTemplate.findAll(FunctionMetadata.class);
        List<String> FunctionNameList = functionMetadataList.stream().map(FunctionMetadata::getFunctionName).collect(Collectors.toList());
        cacheService.saveOrUpdateFunctionName("Function", FunctionNameList);
        AviatorEvaluator.addFunction(new ReplaceFunction());
        AviatorEvaluator.addFunction(new AscFunction());
        AviatorEvaluator.addFunction(new CheckStrLimitFunction());
        AviatorEvaluator.addFunction(new ChrFunction());
        AviatorEvaluator.addFunction(new ContainsFunction());
        AviatorEvaluator.addFunction(new DateFunction());
        AviatorEvaluator.addFunction(new SecondsFunction());
        AviatorEvaluator.addFunction(new DelocalizeExpressionFunction());
        AviatorEvaluator.addFunction(new ExpFunction());
        AviatorEvaluator.addFunction(new FindFunction());
        AviatorEvaluator.addFunction(new FindIndexFunction());
        AviatorEvaluator.addFunction(new FindOffsetFunction());
        AviatorEvaluator.addFunction(new FindPatternFunction());
        AviatorEvaluator.addFunction(new GetArrayBoundsFunction());
        AviatorEvaluator.addFunction(new GetNumElementsFunction());
        AviatorEvaluator.addFunction(new IndexToOffsetFunction());
        AviatorEvaluator.addFunction(new InsertElementsFunction());
        AviatorEvaluator.addFunction(new LeftFunction());
        AviatorEvaluator.addFunction(new LocalizedDecimalPointFunction());
        AviatorEvaluator.addFunction(new LocalizeExpressionFunction());
        AviatorEvaluator.addFunction(new MatchPatternFunction());
        AviatorEvaluator.addFunction(new MidFunction());
        AviatorEvaluator.addFunction(new OffsetToIndexFunction());
        AviatorEvaluator.addFunction(new RandomFunction());
        AviatorEvaluator.addFunction(new RemoveElementsFunction());
        AviatorEvaluator.addFunction(new ReplaceFunction());
        AviatorEvaluator.addFunction(new ResStrFunction());
        AviatorEvaluator.addFunction(new RightFunction());
        AviatorEvaluator.addFunction(new RoundFunction());
        AviatorEvaluator.addFunction(new SearchAndReplaceFunction());
        AviatorEvaluator.addFunction(new SearchPatternAndReplaceFunction());
        AviatorEvaluator.addFunction(new SecondsFunction());
        AviatorEvaluator.addFunction(new SetArrayBoundsFunction());
        AviatorEvaluator.addFunction(new SetElementsFunction());
        AviatorEvaluator.addFunction(new SetNumElementsFunction());
        AviatorEvaluator.addFunction(new SortFunction());
        AviatorEvaluator.addFunction(new SplitFunction());
        AviatorEvaluator.addFunction(new StrCompFunction());
        AviatorEvaluator.addFunction(new StrFunction());
        AviatorEvaluator.addFunction(new TimeFunction());
        AviatorEvaluator.addFunction(new ToLowerFunction());
        AviatorEvaluator.addFunction(new ToUpperFunction());
        AviatorEvaluator.addFunction(new TrimEndFunction());
        AviatorEvaluator.addFunction(new TrimFunction());
        AviatorEvaluator.addFunction(new TrimStartFunction());
        AviatorEvaluator.addFunction(new UInt64Function());
        AviatorEvaluator.addFunction(new ValFunction());
        AviatorEvaluator.addFunction(new CalculateRelativeDistance());
        AviatorEvaluator.addFunction(new FilterNewGenerationFunction());
        AviatorEvaluator.addFunction(new MaxFunction());
        AviatorEvaluator.addFunction(new MinFunction());

    }
}

