import ij.*;
import ij.plugin.PlugIn;
import ij.process.*;
import ij.io.*;
import ij.gui.*;
import ij.plugin.frame.RoiManager;

import java.io.File;

public class Roi_ implements PlugIn {
	public void run(String arg) {

	    DirectoryChooser DiretorioDeEntrada = new DirectoryChooser("Escolha o diretório de origem");
	    String caminhoDiretorioEntrada = DiretorioDeEntrada.getDirectory();
	    if (caminhoDiretorioEntrada == null) return;

	    DirectoryChooser diretorioDeSaida = new DirectoryChooser("Escolha o diretório de destino");
	    String caminhoDiretorioSaida = diretorioDeSaida.getDirectory();
	    if (caminhoDiretorioSaida == null) return;

	    File pastaEntrada = new File(caminhoDiretorioEntrada);
	    File[] listaDeArquivos = pastaEntrada.listFiles();

	    if (listaDeArquivos == null) {
	        IJ.error("Diretório inválido.");
	        return;
	    }

	    RoiManager rm = RoiManager.getInstance();
	    if (rm == null)
	        rm = new RoiManager();

	    for (File arquivo : listaDeArquivos) {

	        if (!arquivo.isFile()) continue;

	        String caminho = arquivo.getAbsolutePath();
	        ImagePlus imagemOriginal = IJ.openImage(caminho);
	        if (imagemOriginal == null) continue;

	        ImagePlus copiaParaProcessar = imagemOriginal.duplicate();

	        IJ.run(copiaParaProcessar, "8-bit", "");

	        //IJ.setAutoThreshold(copiaParaProcessar, "Triangle");
	        //IJ.setAutoThreshold(copiaParaProcessar, "Otsu");
	        IJ.setAutoThreshold(copiaParaProcessar, "Default");
	        IJ.run(copiaParaProcessar, "Convert to Mask", "");

	        IJ.run(copiaParaProcessar, "Fill Holes", "");

	        rm.reset();

	        IJ.run(copiaParaProcessar, "Analyze Particles...",
	                "size=100-Infinity show=Nothing add");

	        Roi[] rois = rm.getRoisAsArray();

	        String baseName = arquivo.getName().replaceAll("\\.[^.]+$", "");

	        for (int i = 0; i < rois.length; i++) {

	            imagemOriginal.setRoi(rois[i]);

	            ImagePlus roiImage = new ImagePlus(
	                    baseName + "_ROI_" + i,
	                    imagemOriginal.getProcessor().crop()
	            );

	            String savePath = caminhoDiretorioSaida + baseName + "_ROI_" + i + ".png";

	            IJ.save(roiImage, savePath);
	            roiImage.close();
	        }

	        imagemOriginal.close();
	        copiaParaProcessar.close();
	    }

	    IJ.showMessage("Processamento concluído!");
	}
}