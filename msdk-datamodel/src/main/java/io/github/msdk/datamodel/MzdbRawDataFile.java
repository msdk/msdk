package io.github.msdk.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

public class MzdbRawDataFile implements RawDataFile {
    
    private @Nonnull String rawDataFileName;
    private @Nonnull Optional<File> originalRawDataFile;
    private @Nonnull FileType rawDataFileType;
    private final @Nonnull ArrayList<MsScan> scans;
    private final @Nonnull ArrayList<Chromatogram> chromatograms;

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<File> getOriginalFile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileType getRawDataFileType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getMsFunctions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MsScan> getScans() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Chromatogram> getChromatograms() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }

}
