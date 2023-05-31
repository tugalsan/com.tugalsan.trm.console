package com.tugalsan.trm.console;

import com.tugalsan.api.callable.client.TGS_CallableType2;
import com.tugalsan.api.charset.client.TGS_CharSetCast;
import com.tugalsan.api.console.server.TS_ConsoleUtils;
import com.tugalsan.api.file.pom.server.TS_FilePom;
import com.tugalsan.api.file.server.TS_DirectoryUtils;
import com.tugalsan.api.file.server.TS_FileUtils;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.os.server.TS_OsJavaUtils;
import com.tugalsan.api.os.server.TS_OsProcessUtils;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import static java.lang.System.out;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

public class Main {

    final private static TS_Log d = TS_Log.of(true, Main.class);

    //cd C:\me\codes\com.tugalsan\trm\com.tugalsan.trm.trm
    //java --enable-preview --add-modules jdk.incubator.concurrent -jar target/com.tugalsan.trm.trm-1.0-SNAPSHOT-jar-with-dependencies.jar
    public static void main(String... args) {
        TGS_CallableType2<Boolean, List<String>, String> tokens_cmdEquals = (tokens, cmd) -> TGS_CharSetCast.equalsLocaleIgnoreCase(tokens.get(0), cmd);
        TGS_CallableType2<Boolean, List<String>, String> tokens_cmdEndsWith = (tokens, cmd) -> TGS_CharSetCast.endsWithLocaleIgnoreCase(tokens.get(0), cmd);
        TS_ConsoleUtils.mainLoop(List.of("q", "quit"), tokens -> {
            if (tokens.isEmpty()) {
                return;
            }
            var firstArg = tokens.get(0);
            var restArgs = TGS_StreamUtils.toLst(
                    IntStream.range(0, tokens.size())
                            .filter(i -> i != 0)
                            .mapToObj(i -> tokens.get(i))
                            .map(s -> (CharSequence) s)
            );
            if (tokens_cmdEquals.call(tokens, "cls")) {
                //NOTHING TODO
                return;
            }
            if (tokens_cmdEndsWith.call(tokens, ".jar")) {
                _jar(firstArg, restArgs);
                return;
            }
            if (tokens_cmdEquals.call(tokens, "lst")) {
                lst(restArgs);
                return;
            }
            if (tokens_cmdEquals.call(tokens, "dep")) {
                dep(restArgs);
                return;
            }
            uknwn(firstArg, restArgs);
        }, args);
    }

    private static void _jar(String firstArg, List<CharSequence> restArgs) {
        d.ci("_jar", "firstArg", firstArg);
        var jarFilePath = Path.of(firstArg);
        if (!TS_FileUtils.isExistFile(jarFilePath)) {
            d.ce("_jar", "ERROR: file not exists @ " + firstArg);
            return;
        }
        TS_OsProcessUtils.runJar(jarFilePath, restArgs);
    }

    private static void uknwn(String firstArg, List<CharSequence> restArgs) {
        d.ce("uknwn", "ERROR: dont know what 2 do with args:");
        d.ci("uknwn", "firstArg", firstArg);
        IntStream.range(0, restArgs.size()).forEachOrdered(i -> {
            d.ci("uknwn", "restArgs", i, restArgs.get(i));
        });
    }

    private static void dep(List<CharSequence> restArgs) {
        var com_tugalsan = TS_OsJavaUtils.getJarPath().getParent().getParent().getParent().getParent();
        if (restArgs.size() != 2) {
            d.ce("dep", "restArgs.size() != 2");
            return;
        }
        var subDirName = restArgs.get(0);
        var prjDirName = restArgs.get(1);
        var subDir = com_tugalsan.resolve(subDirName.toString());
        var prjDir = subDir.resolve(prjDirName.toString());
        var prjPom = prjDir.resolve("pom.xml");
        var pom = TS_FilePom.of(prjPom);
        if (!pom.isLoadedSuccessfully) {
            d.cr("dep", "!pom.isLoadedSuccessfully @ " + pom.articactId);
            return;
        }
        pom.dependenciesFull.forEach(dep -> {
            out.println("- " + dep.articactId);
        });

    }

    private static void lst(List<CharSequence> restArgs) {
        var com_tugalsan = TS_OsJavaUtils.getJarPath().getParent().getParent().getParent().getParent();
        if (restArgs.isEmpty()) {
            lst_printSubDirs(com_tugalsan);
            return;
        }
        var subDirName = restArgs.get(0);
        var subDir = com_tugalsan.resolve(subDirName.toString());
        if (!TS_DirectoryUtils.isExistDirectory(subDir)) {
            d.ce("lst", "ERROR: subDir not exists @ " + subDir);
            return;
        }
        lst_printSubDirs(subDir);
    }

    private static void lst_printSubDirs(Path parentDir) {
        d.cr("lst_printSubDirs", TS_DirectoryUtils.getName(parentDir) + ":");
        TS_DirectoryUtils.subDirectories(parentDir, true, false).forEach(subDir -> {
            d.cr("lst_printSubDirs", "- " + TS_DirectoryUtils.getName(subDir));
        });
    }

}
