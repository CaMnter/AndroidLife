package com.camnter.gradle.plugin.dex.method.counts.jar;

import com.android.dexdeps.DexData;
import com.android.dexdeps.DexDataException;
import com.camnter.gradle.plugin.dex.method.counts.DexCount;
import com.camnter.gradle.plugin.dex.method.counts.DexFieldCounts;
import com.camnter.gradle.plugin.dex.method.counts.DexMethodCounts;
import com.camnter.gradle.plugin.dex.method.counts.struct.Filter;
import com.camnter.gradle.plugin.dex.method.counts.struct.OutputStyle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public class Main {

    private boolean countFields;
    private boolean includeClasses;
    private String packageFilter;
    private int maxDepth = Integer.MAX_VALUE;
    private Filter filter = Filter.ALL;
    private OutputStyle outputStyle = OutputStyle.TREE;


    public static void main(String[] args) {
        Main main = new Main();
        main.run(args);
    }


    @SuppressWarnings("WeakerAccess")
    void run(String[] args) {
        try {
            /**
             * 解析 参数
             * 获取一些解析选项 和 路径
             */
            String[] inputFileNames = parseArgs(args);
            int overallCount = 0;
            /**
             * 遍历所有传入的路径
             * 获取每个路径文件（ dex or apk ），文件内的每个 （ dex ）
             *
             * 遍历每个 dex 数据
             * 通过 google 的 api，实例化 DexData
             * 通过 google 的 api，生成 所有 dex 数据（ class，package，maxDepth and filter ）
             *
             * 输出方法数信息
             *
             * 获取方法数
             */
            for (String fileName : collectFileNames(inputFileNames)) {
                System.out.println("Processing " + fileName);
                DexCount counts;
                if (countFields) {
                    counts = new DexFieldCounts(outputStyle);
                } else {
                    counts = new DexMethodCounts(outputStyle);
                }
                // 路径文件（ dex or apk ），文件内的每个 （ dex ）
                List<RandomAccessFile> dexFiles = openInputFiles(fileName);

                /**
                 * 遍历每个 dex 数据
                 * 通过 google 的 api，实例化 DexData
                 * 通过 google 的 api，生成 所有 dex 数据（ class，package，maxDepth and filter ）
                 */
                for (RandomAccessFile dexFile : dexFiles) {
                    DexData dexData = new DexData(dexFile);
                    dexData.load();
                    // 计算方法数
                    counts.generate(dexData, includeClasses, packageFilter, maxDepth, filter);
                    dexFile.close();
                }
                // 输出方法数信息
                counts.output();
                // 获取方法数
                overallCount = counts.getOverallCount();
            }
            System.out.println(
                String.format("Overall %s count: %d", countFields ? "field" : "method",
                    overallCount));
        } catch (UsageException ue) {
            usage();
            System.exit(2);
        } catch (IOException ioe) {
            if (ioe.getMessage() != null) {
                System.err.println("Failed: " + ioe);
            }
            System.exit(1);
        } catch (DexDataException dde) {
            /* a message was already reported, just bail quietly */
            System.exit(1);
        }
    }


    /**
     * Opens an input file, which could be a .dex or a .jar/.apk with a
     * classes.dex inside.  If the latter, we extract the contents to a
     * temporary file.
     *
     * 将路径转为 zip
     * 获取 zip 文件中的每个 dex 的数据（  RandomAccessFile ）
     * 返回一个  List<RandomAccessFile>
     */
    List<RandomAccessFile> openInputFiles(String fileName) throws IOException {
        List<RandomAccessFile> dexFiles = new ArrayList<RandomAccessFile>();

        openInputFileAsZip(fileName, dexFiles);
        if (dexFiles.size() == 0) {
            File inputFile = new File(fileName);
            RandomAccessFile dexFile = new RandomAccessFile(inputFile, "r");
            dexFiles.add(dexFile);
        }

        return dexFiles;
    }


    /**
     * Tries to open an input file as a Zip archive (jar/apk) with a
     * "classes.dex" inside.
     *
     * 将 路径 转换为 zip
     *
     * 遍历 zip 文件中的每个 dex
     * 通过 openDexFile 方法获取 zip 文件中的每个 dex 的数据（ RandomAccessFile ）
     * 保存到集合 dexFiles
     */
    void openInputFileAsZip(String fileName, List<RandomAccessFile> dexFiles) throws IOException {
        ZipFile zipFile;

        // Try it as a zip file.
        try {
            zipFile = new ZipFile(fileName);
        } catch (FileNotFoundException fnfe) {
            // not found, no point in retrying as non-zip.
            System.err.println("Unable to open '" + fileName + "': " +
                fnfe.getMessage());
            throw fnfe;
        } catch (ZipException ze) {
            // not a zip
            return;
        }

        // Open and add all files matching "classes.*\.dex" in the zip file.
        for (ZipEntry entry : Collections.list(zipFile.entries())) {
            if (entry.getName().matches("classes.*\\.dex")) {
                dexFiles.add(openDexFile(zipFile, entry));
            }
        }

        zipFile.close();
    }


    /**
     * 读取 zip 中的 dex 数据
     *
     * 其实就是创建了一个 new dex
     * 然后用 RandomAccessFile 把 zip 中的 dex 的数据写到 new dex 上
     * 最后删除这个 new dex，因为数据已经读完到了 RandomAccessFile 上
     * 不需要这个 new dex
     *
     * 返回的是 RandomAccessFile，已经将 zip 的数据读完了
     *
     * @param zipFile zipFile
     * @param entry entry
     * @return RandomAccessFile
     * @throws IOException IOException
     */
    RandomAccessFile openDexFile(ZipFile zipFile, ZipEntry entry) throws IOException {
        // We know it's a zip; see if there's anything useful inside.  A
        // failure here results in some type of IOException (of which
        // ZipException is a subclass).
        InputStream zis = zipFile.getInputStream(entry);

        // Create a temp file to hold the DEX data, open it, and delete it
        // to ensure it doesn't hang around if we fail.
        File tempFile = File.createTempFile("dexdeps", ".dex");
        RandomAccessFile dexFile = new RandomAccessFile(tempFile, "rw");
        tempFile.delete();

        // Copy all data from input stream to output file.
        byte copyBuf[] = new byte[32768];
        int actual;

        while (true) {
            actual = zis.read(copyBuf);
            if (actual == -1) {
                break;
            }

            dexFile.write(copyBuf, 0, actual);
        }

        dexFile.seek(0);

        return dexFile;
    }


    private String[] parseArgs(String[] args) {
        int idx;

        for (idx = 0; idx < args.length; idx++) {
            String arg = args[idx];

            if (arg.equals("--") || !arg.startsWith("--")) {
                break;
            } else if (arg.equals("--count-fields")) {
                countFields = true;
            } else if (arg.equals("--include-classes")) {
                includeClasses = true;
            } else if (arg.startsWith("--package-filter=")) {
                packageFilter = arg.substring(arg.indexOf('=') + 1);
            } else if (arg.startsWith("--max-depth=")) {
                maxDepth =
                    Integer.parseInt(arg.substring(arg.indexOf('=') + 1));
            } else if (arg.startsWith("--filter=")) {
                filter = Enum.valueOf(
                    Filter.class,
                    arg.substring(arg.indexOf('=') + 1).toUpperCase());
            } else if (arg.startsWith("--output-style")) {
                outputStyle = Enum.valueOf(
                    OutputStyle.class,
                    arg.substring(arg.indexOf('=') + 1).toUpperCase());
            } else {
                System.err.println("Unknown option '" + arg + "'");
                throw new UsageException();
            }
        }

        // We expect at least one more argument (file name).
        int fileCount = args.length - idx;
        if (fileCount == 0) {
            throw new UsageException();
        }
        String[] inputFileNames = new String[fileCount];
        System.arraycopy(args, idx, inputFileNames, 0, fileCount);
        return inputFileNames;
    }


    private void usage() {
        System.err.print(
            "DEX per-package/class method counts v1.5\n" +
                "Usage: dex-method-counts [options] <file.{dex,apk,jar,directory}> ...\n" +
                "Options:\n" +
                "  --count-fields\n" +
                "  --include-classes\n" +
                "  --package-filter=com.foo.bar\n" +
                "  --max-depth=N\n" +
                "  --filter=ALL|DEFINED_ONLY|REFERENCED_ONLY\n" +
                "  --output-style=FLAT|TREE\n"
        );
    }


    /**
     * Checks if input files array contain directories and
     * adds it's contents to the file list if so.
     * Otherwise just adds a file to the list.
     *
     * @return a List of file names to process
     */
    private List<String> collectFileNames(String[] inputFileNames) {
        List<String> fileNames = new ArrayList<String>();
        for (String inputFileName : inputFileNames) {
            File file = new File(inputFileName);
            if (file.isDirectory()) {
                String dirPath = file.getAbsolutePath();
                for (String fileInDir : file.list()) {
                    fileNames.add(dirPath + File.separator + fileInDir);
                }
            } else {
                fileNames.add(inputFileName);
            }
        }
        return fileNames;
    }


    private static class UsageException extends RuntimeException {}

}