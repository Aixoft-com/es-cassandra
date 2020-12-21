package com.aixoft.escassandra.benchmark.runner;

import ch.qos.logback.classic.Level;
import com.aixoft.escassandra.benchmark.config.BenchmarkEsCassandraConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;

@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 5)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BenchmarkEsCassandraConfiguration.class)
public class BenchmarkWithContext {

    @Test
    void runBenchmark() throws RunnerException {

        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);

        File resultDir = new File("target/benchmark/");
        if(!resultDir.exists()) {
            resultDir.mkdir();
        }

        Options jmhRunnerOptions = new OptionsBuilder()
            .include(getClass().getName())
            .forks(0)
            .threads(1)
            .shouldDoGC(true)
            .shouldFailOnError(true)
            .resultFormat(ResultFormatType.JSON)
            .result(String.format("%s/%s.json", resultDir.getAbsolutePath(), getClass().getName()))
            .shouldFailOnError(true)
            .build();

        Runner runner = new Runner(jmhRunnerOptions);
        runner.run();
    }

}
