package id.zelory.compressor;

import android.content.Context;
import android.graphics.Bitmap;
import io.reactivex.Flowable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created on : June 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class Compressor {

    //max width and height values of the compressed image is taken as 612x816

    /**
     * 压缩允许的最大宽度 612
     */
    private int maxWidth = 612;
    /**
     * 压缩允许的最大高度 816
     */
    private int maxHeight = 816;
    /**
     * 默认格式 JPEG
     */
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    /**
     * 默认质量 80
     */
    private int quality = 80;
    /**
     * 默认输出文件夹
     */
    private String destinationDirectoryPath;


    /**
     * 构造 - 初始化输出文件夹
     *
     * @param context context
     */
    public Compressor(Context context) {
        destinationDirectoryPath = context.getCacheDir().getPath() + File.separator + "images";
    }


    /**
     * 设置最大宽度
     *
     * @param maxWidth maxWidth
     * @return Compressor
     */
    public Compressor setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }


    /**
     * 设置最大高度
     *
     * @param maxHeight maxHeight
     * @return Compressor
     */
    public Compressor setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }


    /**
     * 设置输出格式
     *
     * @param compressFormat compressFormat
     * @return Compressor
     */
    public Compressor setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
        return this;
    }


    /**
     * 设置压缩质量
     *
     * @param quality quality
     * @return Compressor
     */
    public Compressor setQuality(int quality) {
        this.quality = quality;
        return this;
    }


    /**
     * 设置 输出文件化路径
     *
     * @param destinationDirectoryPath destinationDirectoryPath
     * @return Compressor
     */
    public Compressor setDestinationDirectoryPath(String destinationDirectoryPath) {
        this.destinationDirectoryPath = destinationDirectoryPath;
        return this;
    }


    /**
     * 压缩文件
     *
     * @param imageFile imageFile
     * @return File
     * @throws IOException IOException
     */
    public File compressToFile(File imageFile) throws IOException {
        return compressToFile(imageFile, imageFile.getName());
    }


    /**
     * 压缩文件
     *
     * 压格式 JPEG 和 质量
     *
     * @param imageFile imageFile
     * @param compressedFileName compressedFileName
     * @return File
     * @throws IOException IOException
     */
    public File compressToFile(File imageFile, String compressedFileName) throws IOException {
        return ImageUtil.compressImage(imageFile, maxWidth, maxHeight, compressFormat, quality,
            destinationDirectoryPath + File.separator + compressedFileName);
    }


    /**
     * 压缩文件
     *
     * 不压格式 JPEG 和 质量
     *
     * @param imageFile imageFile
     * @return Bitmap
     * @throws IOException IOException
     */
    public Bitmap compressToBitmap(File imageFile) throws IOException {
        return ImageUtil.decodeSampledBitmapFromFile(imageFile, maxWidth, maxHeight);
    }


    /**
     * 压缩文件 转为 Flowable<File>
     *
     * 压格式 JPEG 和 质量
     *
     * @param imageFile imageFile
     * @return Flowable<File>
     */
    public Flowable<File> compressToFileAsFlowable(final File imageFile) {
        return compressToFileAsFlowable(imageFile, imageFile.getName());
    }


    /**
     * 压缩文件 转为 Flowable<File>
     *
     * 压格式 JPEG 和 质量
     *
     * @param imageFile imageFile
     * @param compressedFileName compressedFileName
     * @return Flowable<File>
     */
    public Flowable<File> compressToFileAsFlowable(final File imageFile, final String compressedFileName) {
        return Flowable.defer(new Callable<Flowable<File>>() {
            @Override
            public Flowable<File> call() {
                try {
                    return Flowable.just(compressToFile(imageFile, compressedFileName));
                } catch (IOException e) {
                    return Flowable.error(e);
                }
            }
        });
    }


    /**
     * 压缩文件 转为 Flowable<File>
     *
     * 不压格式 JPEG 和 质量
     *
     * @param imageFile imageFile
     * @return Flowable<Bitmap>
     */
    public Flowable<Bitmap> compressToBitmapAsFlowable(final File imageFile) {
        return Flowable.defer(new Callable<Flowable<Bitmap>>() {
            @Override
            public Flowable<Bitmap> call() {
                try {
                    return Flowable.just(compressToBitmap(imageFile));
                } catch (IOException e) {
                    return Flowable.error(e);
                }
            }
        });
    }

}