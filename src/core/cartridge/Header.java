package core.cartridge;

import exceptions.InvalidFileException;
import utils.FileReader;

import java.io.EOFException;

/**
 * This class represent the header of a iNES file
 */
class Header {

    final byte prg_rom_chunks;
    final byte chr_rom_chunks;
    final byte mapper1;
    final byte mapper2;
    final byte prg_ram_size;

    /**
     * Load the header from the FileReader
     * after the FileReader is ready to read useful data
     *
     * @param reader the FileReader of the iNES file
     */
    Header(FileReader reader) throws InvalidFileException {
        try {
            char[] name = new char[]{(char) reader.nextByte(), (char) reader.nextByte(), (char) reader.nextByte(), (char) reader.nextByte()};
            prg_rom_chunks = reader.nextByte();
            chr_rom_chunks = reader.nextByte();
            mapper1 = reader.nextByte();
            mapper2 = reader.nextByte();
            prg_ram_size = reader.nextByte();
            reader.readBytes(7);
            if (name[0] == 'N' && name[1] == 'E' && name[2] == 'S' && name[3] == 'M')
                throw new InvalidFileException("NSF file not supported");
            else if (name[3] != 0x1A)
                throw new InvalidFileException("Not a valid iNES File");
        } catch (EOFException e) {
            throw new InvalidFileException("Invalid file size (incomplete header)");
        }
    }
}