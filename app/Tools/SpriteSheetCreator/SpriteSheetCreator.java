
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Comparator;
import java.util.Arrays;

public class SpriteSheetCreator
{
    public static void main(String[] args)
    {
        if(args.length != 4)
        {
            System.out.println("Usage: java SpriteSheetCreator [source_path] [output_file] [num_columns] [num_rows]");
            return;
        }
        
        // Gather input parameters
        String sourceDirectory = args[0];
        String outputFileName = args[1];
        int cols = Integer.parseInt(args[2]);
        int rows = Integer.parseInt(args[3]);
        
        BufferedImage outputImage = null;
        Graphics outputGraphics = null;
        
        // dimensions of the image we will be compressing 
        int singleImageWidth = 0;
        int singleImageHeight = 0;
        
        int imageIndex = 0;
        // Sort by filename
        final File dir = new File(sourceDirectory);
        File files[] = dir.listFiles();
        Arrays.sort(files, new Comparator<File>(){
            @Override
            public int compare(File a, File b)
            {
                return a.getName().compareTo(b.getName());
            }
        });
        
        for (final File fileEntry : files)
        {
            System.out.println("Adding Image: " + fileEntry.getName());
            BufferedImage image = null;
            try
            {
                image = ImageIO.read(fileEntry);
                
                if(outputImage == null)
                {
                    singleImageWidth = image.getWidth();
                    singleImageHeight = image.getHeight();
                
                    // construct the output image of the correct size
                    outputImage = new BufferedImage(
                        singleImageWidth * cols,
                        singleImageHeight * rows,
                        BufferedImage.TYPE_INT_ARGB);
                        
                    outputGraphics = outputImage.getGraphics();
                }
                
                // Render the current image to the output image
                int x = (imageIndex % cols) * singleImageWidth;
                int y = (imageIndex / cols) * singleImageHeight;
                
                outputGraphics.drawImage(image, x, y, new Color(0,0,0,0), null);
            }
            catch(IOException e)
            {
            }
            
            imageIndex++;
        }
        
        if(outputImage != null)
        {
            try
            {
                File outputFile = new File(outputFileName);
                ImageIO.write(outputImage, "png", outputFile);
            }
            catch(IOException e)
            {
                System.out.println("Could not write to output image");
            }
        }
    }
}
