package core.cpu;

import core.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represent the CPU of the NES
 */
public class CPU_6502 {

    private Bus bus;

    private int a = 0x00;
    private int x = 0x00;
    private int y = 0x00;
    private int stkp = 0x00;
    private int status = 0x00;
    private int pc = 0x0000;
    
    private int tmp = 0x0000;

    private int fetched = 0x00;
    private int opcode = 0x00;
    private int cycles = 0x00;
    private int addr_abs = 0x0000;
    private int addr_rel = 0x0000;

    private long cpu_clock = 0L;

    private List<Instruction> opcodes;

    /**
     * Create a new CPU and populate the opcode list
     */
    public CPU_6502() {
        opcodes = new ArrayList<>();
        opcodes.add(new Instruction("BRK", "IMM", 7) { public int operate() { return brk(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("ORA", "IZX", 6) { public int operate() { return ora(); } public int addrmode() { return izx(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 3) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ORA", "ZP0", 3) { public int operate() { return ora(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("ASL", "ZP0", 5) { public int operate() { return asl(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("PHP", "IMP", 3) { public int operate() { return php(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ORA", "IMM", 2) { public int operate() { return ora(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("ASL", "IMP", 2) { public int operate() { return asl(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ORA", "ABS", 4) { public int operate() { return ora(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("ASL", "ABS", 6) { public int operate() { return asl(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BPL", "REL", 2) { public int operate() { return bpl(); } public int addrmode() { return rel(); }});
        opcodes.add(new Instruction("ORA", "IZY", 5) { public int operate() { return ora(); } public int addrmode() { return izy(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ORA", "ZPX", 4) { public int operate() { return ora(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("ASL", "ZPX", 6) { public int operate() { return asl(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CLC", "IMP", 2) { public int operate() { return clc(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ORA", "ABY", 4) { public int operate() { return ora(); } public int addrmode() { return aby(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ORA", "ABX", 4) { public int operate() { return ora(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("ASL", "ABX", 7) { public int operate() { return asl(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("JSR", "ABS", 6) { public int operate() { return jsr(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("AND", "IZX", 6) { public int operate() { return and(); } public int addrmode() { return izx(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BIT", "ZP0", 3) { public int operate() { return bit(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("AND", "ZP0", 3) { public int operate() { return and(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("ROL", "ZP0", 5) { public int operate() { return rol(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("PLP", "IMP", 4) { public int operate() { return plp(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("AND", "IMM", 2) { public int operate() { return and(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("ROL", "IMP", 2) { public int operate() { return rol(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BIT", "ABS", 4) { public int operate() { return bit(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("AND", "ABS", 4) { public int operate() { return and(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("ROL", "ABS", 6) { public int operate() { return rol(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BMI", "REL", 2) { public int operate() { return bmi(); } public int addrmode() { return rel(); }});
        opcodes.add(new Instruction("AND", "IZY", 5) { public int operate() { return and(); } public int addrmode() { return izy(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("AND", "ZPX", 4) { public int operate() { return and(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("ROL", "ZPX", 6) { public int operate() { return rol(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("SEC", "IMP", 2) { public int operate() { return sec(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("AND", "ABY", 4) { public int operate() { return and(); } public int addrmode() { return aby(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("AND", "ABX", 4) { public int operate() { return and(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("ROL", "ABX", 7) { public int operate() { return rol(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("RTI", "IMP", 6) { public int operate() { return rti(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("EOR", "IZX", 6) { public int operate() { return eor(); } public int addrmode() { return izx(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 3) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("EOR", "ZP0", 3) { public int operate() { return eor(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("LSR", "ZP0", 5) { public int operate() { return lsr(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("PHA", "IMP", 3) { public int operate() { return pha(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("EOR", "IMM", 2) { public int operate() { return eor(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("LSR", "IMP", 2) { public int operate() { return lsr(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("JMP", "ABS", 3) { public int operate() { return jmp(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("EOR", "ABS", 4) { public int operate() { return eor(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("LSR", "ABS", 6) { public int operate() { return lsr(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BVC", "REL", 2) { public int operate() { return bvc(); } public int addrmode() { return rel(); }});
        opcodes.add(new Instruction("EOR", "IZY", 5) { public int operate() { return eor(); } public int addrmode() { return izy(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("EOR", "ZPX", 4) { public int operate() { return eor(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("LSR", "ZPX", 6) { public int operate() { return lsr(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CLI", "IMP", 2) { public int operate() { return cli(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("EOR", "ABY", 4) { public int operate() { return eor(); } public int addrmode() { return aby(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("EOR", "ABX", 4) { public int operate() { return eor(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("LSR", "ABX", 7) { public int operate() { return lsr(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("RTS", "IMP", 6) { public int operate() { return rts(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ADC", "IZX", 6) { public int operate() { return adc(); } public int addrmode() { return izx(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 3) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ADC", "ZP0", 3) { public int operate() { return adc(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("ROR", "ZP0", 5) { public int operate() { return ror(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("PLA", "IMP", 4) { public int operate() { return pla(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ADC", "IMM", 2) { public int operate() { return adc(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("ROR", "IMP", 2) { public int operate() { return ror(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("JMP", "IND", 5) { public int operate() { return jmp(); } public int addrmode() { return ind(); }});
        opcodes.add(new Instruction("ADC", "ABS", 4) { public int operate() { return adc(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("ROR", "ABS", 6) { public int operate() { return ror(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BVS", "REL", 2) { public int operate() { return bvs(); } public int addrmode() { return rel(); }});
        opcodes.add(new Instruction("ADC", "IZY", 5) { public int operate() { return adc(); } public int addrmode() { return izy(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ADC", "ZPX", 4) { public int operate() { return adc(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("ROR", "ZPX", 6) { public int operate() { return ror(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("SEI", "IMP", 2) { public int operate() { return sei(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ADC", "ABY", 4) { public int operate() { return adc(); } public int addrmode() { return aby(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("ADC", "ABX", 4) { public int operate() { return adc(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("ROR", "ABX", 7) { public int operate() { return ror(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("STA", "IZX", 6) { public int operate() { return sta(); } public int addrmode() { return izx(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("STY", "ZP0", 3) { public int operate() { return sty(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("STA", "ZP0", 3) { public int operate() { return sta(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("STX", "ZP0", 3) { public int operate() { return stx(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("???", "IMP", 3) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("DEY", "IMP", 2) { public int operate() { return dey(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("TXA", "IMP", 2) { public int operate() { return txa(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("STY", "ABS", 4) { public int operate() { return sty(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("STA", "ABS", 4) { public int operate() { return sta(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("STX", "ABS", 4) { public int operate() { return stx(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BCC", "REL", 2) { public int operate() { return bcc(); } public int addrmode() { return rel(); }});
        opcodes.add(new Instruction("STA", "IZY", 6) { public int operate() { return sta(); } public int addrmode() { return izy(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("STY", "ZPX", 4) { public int operate() { return sty(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("STA", "ZPX", 4) { public int operate() { return sta(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("STX", "ZPY", 4) { public int operate() { return stx(); } public int addrmode() { return zpy(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("TYA", "IMP", 2) { public int operate() { return tya(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("STA", "ABY", 5) { public int operate() { return sta(); } public int addrmode() { return aby(); }});
        opcodes.add(new Instruction("TXS", "IMP", 2) { public int operate() { return txs(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("STA", "ABX", 5) { public int operate() { return sta(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("LDY", "IMM", 2) { public int operate() { return ldy(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("LDA", "IZX", 6) { public int operate() { return lda(); } public int addrmode() { return izx(); }});
        opcodes.add(new Instruction("LDX", "IMM", 2) { public int operate() { return ldx(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("LDY", "ZP0", 3) { public int operate() { return ldy(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("LDA", "ZP0", 3) { public int operate() { return lda(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("LDX", "ZP0", 3) { public int operate() { return ldx(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("???", "IMP", 3) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("TAY", "IMP", 2) { public int operate() { return tay(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("LDA", "IMM", 2) { public int operate() { return lda(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("TAX", "IMP", 2) { public int operate() { return tax(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("LDY", "ABS", 4) { public int operate() { return ldy(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("LDA", "ABS", 4) { public int operate() { return lda(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("LDX", "ABS", 4) { public int operate() { return ldx(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BCS", "REL", 2) { public int operate() { return bcs(); } public int addrmode() { return rel(); }});
        opcodes.add(new Instruction("LDA", "IZY", 5) { public int operate() { return lda(); } public int addrmode() { return izy(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("LDY", "ZPX", 4) { public int operate() { return ldy(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("LDA", "ZPX", 4) { public int operate() { return lda(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("LDX", "ZPY", 4) { public int operate() { return ldx(); } public int addrmode() { return zpy(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CLV", "IMP", 2) { public int operate() { return clv(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("LDA", "ABY", 4) { public int operate() { return lda(); } public int addrmode() { return aby(); }});
        opcodes.add(new Instruction("TSX", "IMP", 2) { public int operate() { return tsx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("LDY", "ABX", 4) { public int operate() { return ldy(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("LDA", "ABX", 4) { public int operate() { return lda(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("LDX", "ABY", 4) { public int operate() { return ldx(); } public int addrmode() { return aby(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CPY", "IMM", 2) { public int operate() { return cpy(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("CMP", "IZX", 6) { public int operate() { return cmp(); } public int addrmode() { return izx(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CPY", "ZP0", 3) { public int operate() { return cpy(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("CMP", "ZP0", 3) { public int operate() { return cmp(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("DEC", "ZP0", 5) { public int operate() { return dec(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("INY", "IMP", 2) { public int operate() { return iny(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CMP", "IMM", 2) { public int operate() { return cmp(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("DEX", "IMP", 2) { public int operate() { return dex(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CPY", "ABS", 4) { public int operate() { return cpy(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("CMP", "ABS", 4) { public int operate() { return cmp(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("DEC", "ABS", 6) { public int operate() { return dec(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BNE", "REL", 2) { public int operate() { return bne(); } public int addrmode() { return rel(); }});
        opcodes.add(new Instruction("CMP", "IZY", 5) { public int operate() { return cmp(); } public int addrmode() { return izy(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CMP", "ZPX", 4) { public int operate() { return cmp(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("DEC", "ZPX", 6) { public int operate() { return dec(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CLD", "IMP", 2) { public int operate() { return cld(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CMP", "ABY", 4) { public int operate() { return cmp(); } public int addrmode() { return aby(); }});
        opcodes.add(new Instruction("NOP", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CMP", "ABX", 4) { public int operate() { return cmp(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("DEC", "ABX", 7) { public int operate() { return dec(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CPX", "IMM", 2) { public int operate() { return cpx(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("SBC", "IZX", 6) { public int operate() { return sbc(); } public int addrmode() { return izx(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CPX", "ZP0", 3) { public int operate() { return cpx(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("SBC", "ZP0", 3) { public int operate() { return sbc(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("INC", "ZP0", 5) { public int operate() { return inc(); } public int addrmode() { return zp0(); }});
        opcodes.add(new Instruction("???", "IMP", 5) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("INX", "IMP", 2) { public int operate() { return inx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("SBC", "IMM", 2) { public int operate() { return sbc(); } public int addrmode() { return imm(); }});
        opcodes.add(new Instruction("NOP", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return sbc(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("CPX", "ABS", 4) { public int operate() { return cpx(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("SBC", "ABS", 4) { public int operate() { return sbc(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("INC", "ABS", 6) { public int operate() { return inc(); } public int addrmode() { return abs(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("BEQ", "REL", 2) { public int operate() { return beq(); } public int addrmode() { return rel(); }});
        opcodes.add(new Instruction("SBC", "IZY", 5) { public int operate() { return sbc(); } public int addrmode() { return izy(); }});
        opcodes.add(new Instruction("???", "IMP", 2) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 8) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("SBC", "ZPX", 4) { public int operate() { return sbc(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("INC", "ZPX", 6) { public int operate() { return inc(); } public int addrmode() { return zpx(); }});
        opcodes.add(new Instruction("???", "IMP", 6) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("SED", "IMP", 2) { public int operate() { return sed(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("SBC", "ABY", 4) { public int operate() { return sbc(); } public int addrmode() { return aby(); }});
        opcodes.add(new Instruction("NOP", "IMP", 2) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 4) { public int operate() { return nop(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("SBC", "ABX", 4) { public int operate() { return sbc(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("INC", "ABX", 7) { public int operate() { return inc(); } public int addrmode() { return abx(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
        opcodes.add(new Instruction("???", "IMP", 7) { public int operate() { return xxx(); } public int addrmode() { return imp(); }});
    }

    /**
     * Connect the CPU to a Bus
     * @param bus the Bus to connect to
     */
    public void connectBus(Bus bus) {
        this.bus = bus;
    }

    /**
     * Write to an address in the addressable range
     * @param addr the address to write to
     */
    private void write(int addr, int data) {
        bus.cpuWrite(addr, data);
    }

    /**
     * Read from an address in the addressable range
     * @param addr the address to read from
     * @return the read data
     */
    private int read(int addr) {
        return bus.cpuRead(addr, false);
    }

    /**
     * Get a Flag value
     * @param flag the Flag to get
     * @return is the Flag set to 1
     */
    private boolean getFlag(Flags flag) {
        return (status & flag.value) == flag.value;
    }

    /**
     * Set a Flag to 0 or 1
     * @param flag The Flag to set
     * @param value should the Flag be set 1
     */
    private void setFlag(Flags flag, boolean value) {
        if (value)
            status |= flag.value;
        else
            status &= ~flag.value;
    }

    // =========================================== Addressing Modes ===========================================

    /**
     * Implied Addressing
     * Their is not data to fetch, the instruction will use the Accumulator as input
     * @return 0 No extra cycle required
     */
    private int imp() {
        fetched = a & 0x00FF;
        return 0;
    }

    /**
     * Zero Page Addressing
     * The value following the OPCode is considered as an offset
     * this offset is used to index the 0th page
     * Example : Passed value      : 0x12
     *           Effective address : 0x0012
     * @return 0 No extra cycle required
     */
    private int zp0() {
        addr_abs = read(pc);
        addr_abs &= 0x00FF;
        pc = (pc+1) & 0xFFFF;
        return 0;
    }

    /**
     * Zero Page Addressing with Y Offset
     * The value following the OPCode is considered as an offset
     * the content of the Y Register is added to that offset
     * this offset is used to index the 0th page
     * Example : Passed value      : 0x12
     *           Y Register        : 0xD
     *           Effective address : 0x00E2
     * @return 0 No extra cycle required
     */
    private int zpy() {
        addr_abs = read(pc) + (y & 0x00FF);
        addr_abs &= 0x00FF;
        pc = (pc+1) & 0xFFFF;
        return 0;
    }

    /**
     * Absolute Addressing
     * The address is read as the following 16bit
     * @return 0 No extra cycle required
     */
    private int abs() {
        int low = read(pc);
        pc = (pc+1) & 0xFFFF;
        int high = read(pc);
        pc = (pc+1) & 0xFFFF;
        addr_abs =  (high << 8) | low;
        return 0;
    }

    /**
     * Absolute Addressing with Y Offset
     * The Address is read as the following 16bit
     * the Y Register is then adder to that Address
     * @return 1 if a page Boundary is crossed when adding Y Register, 0 otherwise
     */
    private int aby() {
        int low = read(pc);
        pc = (pc+1) & 0xFFFF;
        int high = read(pc);
        pc = (pc+1) & 0xFFFF;
        addr_abs = (high << 8 | low) & 0xFFFF;
        addr_abs += y & 0x00FF;
        addr_abs &= 0xFFFF;
        if ((addr_abs & 0xFF00) != (high << 8))
            return 1;
        return 0;
    }

    /**
     * Indirect X Addressing
     * Read the address next to the OPCode and index in the 0th page
     * The X Register is then added to that address
     * We then load a 16bit value from that offset address (8 LSB read followed by 8 MSB)
     * Example : Passed address      : 0xD8
     *           value in X Register : 0x10
     *           0th page address    : 0x00E8
     *           value at 0x00E8     : 0x12
     *           value at 0x00E9     : 0x23
     *           effective address   : 0x2312
     * @return 0 No extra cycle required
     */
    private int izx() {
        int ptr = read(pc);
        pc = (pc+1) & 0xFFFF;
        int low = read((ptr + x) & 0x00FF);
        int high = read((ptr + x + 1) & 0x00FF);
        addr_abs =  (high << 8) | low;
        return 0;
    }

    /**
     * Immediate Addressing
     * The value following the OPCode is the value searched
     * the effective address is then the Program Counter
     * @return 0 No extra cycle required
     */
    private int imm() {
        addr_abs = pc;
        pc =(pc+1) & 0xFFFF;
        return 0;
    }

    /**
     * Zero Page Addressing with X Offset
     * The value following the OPCode is considered as an offset
     * the content of the X Register is added to that offset
     * this offset is used to index the 0th page
     * Example : Passed value      : 0x12
     *           X Register        : 0xD
     *           Effective address : 0x00E2
     * @return 0 No extra cycle required
     */
    private int zpx() {
        addr_abs =  (read(pc) + (x & 0x00FF));
        addr_abs &= 0x00FF;
        pc = (pc+1) & 0xFFFF;
        return 0;
    }

    /**
     * Relative Addressing (Exclusive to Branching instruction)
     * The value following the OPCode is considered as a signed 8bit value
     * that value is then added (as signed) to the current address
     * @return 0 No extra cycle required
     */
    private int rel() {
        addr_rel = read(pc);
        pc = (pc+1) & 0xFFFF;
        if ((addr_rel & 0x80) != 0x0000)
            addr_rel |= 0xFFFFFF00;
        return 0;
    }

    /**
     * Absolute Addressing with X Offset
     * The address is read as the following 16bit
     * the X Register is then adder to that Address
     * @return 1 if a page boundary is crossed when adding X Register, 0 otherwise
     */
    private int abx() {
        int low = read(pc);
        pc = (pc+1) & 0xFFFF;
        int high = read(pc);
        pc = (pc+1) & 0xFFFF;
        addr_abs =  ((high << 8) | low) + (x & 0x00FF);
        if ((addr_abs & 0xFF00) != (high << 8))
            return 1;
        return 0;
    }

    /**
     * Indirect Addressing
     * an address is read from the 16bit following the OPCode (8 LSB then 8 MSB)
     * We then read the Address at that location (8 LSB followed by 8 MSB)
     *
     * This Addressing mode contains a bug in real hardware
     * If the address following the OPCode has the 8 LSB equals to 0xFF
     * then when reading the effective address the page boundary isn't crossed
     * it then read the first address of the same page
     *
     * Examples : Passed addresses  : 0x1FF3     0x1DFF
     *            value at 0x1FF3   : 0x12
     *            value at 0x1FF4   : 0x23
     *            value at 0x1DFF   : 0x32
     *            value at 0x1D00   : 0xA1
     *            effective address : 0x2312     0xA132  *The 8 MSB are read from 0x1D00 instead of 1E00
     * @return 0 No extra cycle required (because no page boundary cross can occur)
     */
    private int ind() {
        int low = read(pc);
        pc = (pc+1) & 0xFFFF;
        int high = read(pc);
        pc = (pc+1) & 0xFFFF;
        int ptr =  (high << 8) | low;

        if (low == 0xFF) //Page Boundary bug
            addr_abs =  (read((ptr & 0xFF00) << 8)) | read(ptr);
        else
            addr_abs = (read(ptr + 1) << 8) | read(ptr);
        return 0;
    }

    /**
     * Indirect Y Addressing
     * Read the address next to the OPCode and index in the 0th page
     * We then load a 16bit value from that address (8 LSB followed by 8 MSB)
     * The Y Register is then added to get the final address
     * Example : Passed address      : 0xF8
     *           0th page address    : 0x00F8
     *           value at 0x00F8     : 0x12
     *           value at 0x00F9     : 0x23
     *           value in Y Register : 0x10
     *           read Address        : 0x2312
     *           effective address   : 0x2322
     * @return 1 If when adding Y we cross a page boundary 0 otherwise
     */
    private int izy() {
        int ptr = read(pc);
        pc = (pc+1) & 0xFFFF;

        int low = read(ptr & 0x00FF) & 0x00FF;
        int high = read((ptr  + 1) & 0x00FF) & 0x00FF;
        addr_abs = ((high << 8) | low ) & 0xFFFF;
        addr_abs += y & 0x00FF;
        addr_abs &= 0xFFFF;
        if ((addr_abs & 0xFF00) != (high << 8))
            return 1;
        return 0;
    }


    // ================================================ Opcodes ================================================
    /**
     * Add the fetched value to the Accumulator
     * C Flag set if Accumulator + Fetched > 0xFF
     * Z Flag set if the final Accumulator = 0
     * N Flag set if the final Accumulator has MSB set
     * V Flag set if (When considering data as signed) :
     *      Positive + Positive = Negative (MSB set)
     *      Negative + Negative = Positive (MSB not set)
     * @return 1 An extra cycle can be required depending on the addressing mode
     */
    private int adc() {
        fetch();
        tmp =  (a + fetched + (getFlag(Flags.C) ? 0x1 : 0x0)) & 0x01FF;
        setFlag(Flags.C, tmp > 0x00FF);
        setFlag(Flags.Z, (tmp & 0x00FF) == 0);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        setFlag(Flags.V, ((~(a ^ fetched) & (a ^ tmp)) & 0x0080) == 0x0080);
        a =  (tmp & 0x00FF);
        return 1;
    }

    /**
     * Compute a Logic AND between the Accumulator and the Fetched data
     * stores it into the Accumulator
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 1 Extra cycle may be required
     */
    private int and() {
        fetch();
        a =  (a & fetched) & 0x00FF;
        setFlag(Flags.Z, a == 0x00);
        setFlag(Flags.N, (a & 0x80) != 0x00);
        return 1;
    }

    /**
     * Shift Left the Fetched data and store it depending on the addressing mode
     * C Flag set if Fetched data has MSB set
     * Z Flag set if Computed data = 0
     * N Flag set if Computed data has MSB set
     * @return 0 No extra cycle required
     */
    private int asl() {
        fetch();
        tmp =  (fetched << 1) & 0x01FF;
        setFlag(Flags.C, (tmp & 0xFF00) > 0);
        setFlag(Flags.Z, (tmp & 0x00FF) == 0x0000);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        if (opcodes.get(opcode).addr_mode.equals("IMP"))
            a =  (tmp & 0x00FF);
        else
            write(addr_abs,  (tmp & 0x00FF));
        return 0;
    }

    /**
     * Branch on Carry Clear
     * Jump to Fetched Address if C Flag isn't set
     * @return 0 No extra cycle required
     */
    private int bcc() {
        if (!getFlag(Flags.C)) {
            cycles++;
            addr_abs = pc + addr_rel;
            if ((addr_abs & 0xFF00) != (pc & 0xFF00))
                cycles++;
            pc = addr_abs & 0xFFFF;
        }
        return 0;
    }

    /**
     * Branch on Carry Set
     * Jump to Fetched Address if C Flag is set
     * @return 0 No extra cycle required
     */
    private int bcs() {
        if (getFlag(Flags.C)) {
            cycles++;
            addr_abs = pc + addr_rel;
            if ((addr_abs & 0xFF00) != (pc & 0xFF00))
                cycles++;
            pc = addr_abs & 0xFFFF;
        }
        return 0;
    }

    /**
     * Branch on Equal
     * Jump to Fetched Address if Z Flag is set
     * @return 0 No extra cycle required
     */
    private int beq() {
        if (getFlag(Flags.Z)) {
            cycles++;
            addr_abs = pc + addr_rel;
            if ((addr_abs & 0xFF00) != (pc & 0xFF00))
                cycles++;
            pc = addr_abs & 0xFFFF;
        }
        return 0;
    }

    /**
     * Do a Bit test between the Accumulator and the Fetched data
     * Z Flag set if the Accumulator and the Fetched data have no common bits
     * N Flag set if Fetched data has 7th bit set
     * V Flag set if Fetched data has 6th bit set
     * @return 0 No extra cycle required
     */
    private int bit() {
        fetch();
        tmp =  (a & fetched) & 0x00FF;
        setFlag(Flags.Z, (tmp & 0x00FF) == 0x0000);
        setFlag(Flags.N, (fetched & (1 << 7)) == (1 << 7));
        setFlag(Flags.V, (fetched & (1 << 6)) == (1 << 6));
        return 0;
    }

    /**
     * Branch on Negative
     * Jump to Fetched Address if N Flag is set
     * @return 0 No extra cycle required
     */
    private int bmi() {
        if (getFlag(Flags.N)) {
            cycles++;
            addr_abs = pc + addr_rel;
            if ((addr_abs & 0xFF00) != (pc & 0xFF00))
                cycles++;
            pc = addr_abs & 0xFFFF;
        }
        return 0;
    }

    /**
     * Branch on Not Equal
     * Jump to Fetched Address if Z Flag isn't set
     * @return 0 No extra cycle required
     */
    private int bne() {
         if (!getFlag(Flags.Z)) {
            cycles++;
            addr_abs = pc + addr_rel;
            if ((addr_abs & 0xFF00) != (pc & 0xFF00))
                cycles++;
            pc = addr_abs & 0xFFFF;
        }
        return 0;
    }

    /**
     * Branch on Positive
     * Jump to Fetched Address if N Flag isn't set
     * @return 0 No extra cycle required
     */
    private int bpl() {
        if (!getFlag(Flags.N)) {
            cycles++;
            addr_abs = pc + addr_rel;
            if ((addr_abs & 0xFF00) != (pc & 0xFF00))
                cycles++;
            pc = addr_abs & 0xFFFF;
        }
        return 0;
    }

    /**
     * Fire an Interrupt
     * Push the Program Counter and Status Register (Set the B Flag before) to the Stack
     * Jump to the Address Specified at Location 0xFFFF and 0xFFFE
     * @return 0 No extra cycle required
     */
    private int brk() {
        pc = (pc+1) & 0xFFFF;
        setFlag(Flags.I, true);
        write(0x0100 + stkp,  ((pc >> 8) & 0x00FF));
        stkp = (stkp-1) & 0x00FF;
        write(0x0100 + stkp,  (pc & 0x00FF));
        stkp = (stkp-1) & 0x00FF;
        setFlag(Flags.B, true);
        write(0x0100 + stkp, status);
        stkp = (stkp-1) & 0x00FF;
        setFlag(Flags.B, false);
        pc = read(0xFFFE) | (read(0xFFFF)<< 8);
        return 0;
    }

    /**
     * Branch on Not Overflow
     * Jump to Fetched Address if V Flag isn't set
     * @return 0 No extra cycle required
     */
    private int bvc() {
        if (!getFlag(Flags.V)) {
            cycles++;
            addr_abs = pc + addr_rel;
            if ((addr_abs & 0xFF00) != (pc & 0xFF00))
                cycles++;
            pc = addr_abs & 0xFFFF;
        }
        return 0;
    }

    /**
     * Branch on Overflow
     * Jump to Fetched Address if V Flag is set
     * @return 0 No extra cycle required
     */
    private int bvs() {
        if (getFlag(Flags.V)) {
            cycles++;
            addr_abs = pc + addr_rel;
            if ((addr_abs & 0xFF00) != (pc & 0xFF00))
                cycles++;
            pc = addr_abs & 0xFFFF;
        }
        return 0;
    }

    /**
     * Set the Carry Flag of the Status Register to 0
     * C Flag not set
     * @return 0 No extra cycle required
     */
    private int clc() {
        setFlag(Flags.C, false);
        return 0;
    }

    /**
     * Set the Decimal Flag of the Status Register to 0
     * D Flag not set
     * @return 0 No extra cycle required
     */
    private int cld() {
        setFlag(Flags.D, false);
        return 0;
    }

    /**
     * Set the Interrupt Flag of the Status Register to 0
     * I Flag not set
     * @return 0 No extra cycle required
     */
    private int cli() {
        setFlag(Flags.I, false);
        return 0;
    }

    /**
     * Set the Overflow Flag of the Status Register to 0
     * V Flag not set
     * @return 0 No extra cycle required
     */
    private int clv() {
        setFlag(Flags.V, false);
        return 0;
    }

    /**
     * Compare the Fetched data with the Accumulator
     * C Flag set if Accumulator > Fetched data
     * Z Flag set if Accumulator = Fetched data
     * N Flag set if Accumulator < Fetched data
     * @return 0 No extra cycle required
     */
    private int cmp() {
        fetch();
        tmp =  (a + (fetched ^ 0x00FF) + 1) & 0x00FF;
        setFlag(Flags.C, a >= fetched);
        setFlag(Flags.Z, (a & 0xFF) == (fetched & 0xFF));
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        return 1;
    }

    /**
     * Compare the Fetched data with the X Register
     * C Flag set if X Register > Fetched data
     * Z Flag set if X Register = Fetched data
     * N Flag set if X Register < Fetched data
     * @return 0 No extra cycle required
     */
    private int cpx() {
        fetch();
        tmp =  (x + (fetched ^ 0x00FF) + 1) & 0x00FF;
        setFlag(Flags.C, x >= fetched);
        setFlag(Flags.Z, (tmp & 0x00FF) == 0x0000);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        return 0;
    }

    /**
     * Compare the Fetched data with the Y Register
     * C Flag set if Y Register > Fetched data
     * Z Flag set if Y Register = Fetched data
     * N Flag set if Y Register < Fetched data
     * @return 0 No extra cycle required
     */
    private int cpy() {
        fetch();
        tmp =  (y + (fetched ^ 0x00FF) + 1) & 0x00FF;
        setFlag(Flags.C, y >= fetched);
        setFlag(Flags.Z, (tmp & 0x00FF) == 0x0000);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        return 0;
    }

    /**
     * Decrement the Accumulator
     * If Accumulator = 0x00 => wrap to 0xFF
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 0 No extra cycle required
     */
    private int dec() {
        fetch();
        tmp =  (fetched - 1) & 0x00FF;
        write(addr_abs,  (tmp & 0x00FF));
        setFlag(Flags.Z, (tmp & 0x00FF) == 0x0000);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        return 0;
    }

    /**
     * Decrement the X Register
     * If X Register = 0x00 => wrap to 0xFF
     * Z Flag set if X Register = 0
     * N Flag set if X Register has MSB set
     * @return 0 No extra cycle required
     */
    private int dex() {
        x = (x-1) & 0x00FF;
        setFlag(Flags.Z, (x & 0x00FF) == 0x0000);
        setFlag(Flags.N, (x & 0x0080) == 0x0080);
        return 0;
    }

    /**
     * Decrement the Y Register
     * If Y Register = 0x00 => wrap to 0xFF
     * Z Flag set if Y Register = 0
     * N Flag set if Y Register has MSB set
     * @return 0 No extra cycle required
     */
    private int dey() {
        y = (y-1) & 0x00FF;
        setFlag(Flags.Z, (y & 0x00FF) == 0x0000);
        setFlag(Flags.N, (y & 0x0080) == 0x0080);
        return 0;
    }

    /**
     * Compute a Logic XOR between the Accumulator and the Fetched data
     * stores it into the Accumulator
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 1 Extra cycle may be required
     */
    private int eor() {
        fetch();
        a =  (a ^ fetched) & 0x00FF;
        setFlag(Flags.Z, (a & 0x00FF) == 0x0000);
        setFlag(Flags.N, (a & 0x0080) == 0x0080);
        return 1;
    }

    /**
     * Increment the Accumulator
     * If Accumulator = 0xFF => wrap to 0x00
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 0 No extra cycle required
     */
    private int inc() {
        fetch();
        tmp =  (fetched + 1) & 0x00FF;
        write(addr_abs,  (tmp & 0x00FF));
        setFlag(Flags.Z, (tmp & 0x00FF) == 0x0000);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        return 0;
    }

    /**
     * Increment the X Register
     * If X Register = 0xFF => wrap to 0x00
     * Z Flag set if X Register = 0
     * N Flag set if X Register has MSB set
     * @return 0 No extra cycle required
     */
    private int inx() {
        x = (x+1) & 0x00FF;
        setFlag(Flags.Z, (x & 0x00FF) == 0x0000);
        setFlag(Flags.N, (x & 0x0080) == 0x0080);
        return 0;
    }

    /**
     * Increment the Y Register
     * If Y Register = 0xFF => wrap to 0x00
     * Z Flag set if Y Register = 0
     * N Flag set if Y Register has MSB set
     * @return 0 No extra cycle required
     */
    private int iny() {
        y = (y+1) & 0x00FF;
        setFlag(Flags.Z, (y & 0x00FF) == 0x0000);
        setFlag(Flags.N, (y & 0x0080) == 0x0080);
        return 0;
    }

    /**
     * Jump to Fetched Address
     * No Flag Update
     * @return 0 No extra cycle required
     */
    private int jmp() {
        pc = addr_abs & 0xFFFF;
        return 0;
    }

    /**
     * Jump to Subroutine
     * Push Program Counter to the Stack and Jump to Fetched Address
     * No Flag Update
     * @return 0 No extra cycle required
     */
    private int jsr() {
        pc = (pc-1) & 0xFFFF;
        write(0x0100 + stkp,  ((pc >> 8) & 0x00FF));
        stkp = (stkp-1) & 0x00FF;
        write(0x0100 + stkp,  (pc & 0x00FF));
        stkp = (stkp-1) & 0x00FF;
        pc = addr_abs & 0xFFFF;
        return 0;
    }

    /**
     * Store the Fetched data into the Accumulator
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 1 Extra cycle may be required depending on the addressing mode
     */
    private int lda() {
        fetch();
        a = fetched & 0x00FF;
        setFlag(Flags.Z, (a & 0x00FF) == 0x0000);
        setFlag(Flags.N, (a & 0x0080) == 0x0080);
        return 1;
    }

    /**
     * Store the Fetched data into the X Register
     * Z Flag set if X Register = 0
     * N Flag set if X Register has MSB set
     * @return 1 Extra cycle may be required depending on the addressing mode
     */
    private int ldx() {
        fetch();
        x = fetched & 0x00FF;
        setFlag(Flags.Z, (x & 0x00FF) == 0x0000);
        setFlag(Flags.N, (x & 0x0080) == 0x0080);
        return 1;
    }

    /**
     * Store the Fetched data into the Y Register
     * Z Flag set if Y Register = 0
     * N Flag set if Y Register has MSB set
     * @return 1 Extra cycle may be required depending on the addressing mode
     */
    private int ldy() {
        fetch();
        y = fetched & 0x00FF;
        setFlag(Flags.Z, (y & 0x00FF) == 0x0000);
        setFlag(Flags.N, (y & 0x0080) == 0x0080);
        return 1;
    }

    /**
     * Shift Right the Fetched data and store it depending on the addressing mode
     * Z Flag set if Computed data = 0
     * N Flag set if Computed data has MSB set
     * @return 0 No extra cycle required
     */
    private int lsr() {
        fetch();
        setFlag(Flags.C, (fetched & 0x0001) == 0x0001);
        tmp =  (fetched >> 1);
        setFlag(Flags.Z, (tmp & 0x00FF) == 0x0000);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        if (opcodes.get(opcode).addr_mode.equals("IMP"))
            a =  (tmp & 0x00FF);
        else
            write(addr_abs,  (tmp & 0x00FF));
        return 0;
    }

    /**
     * Do nothing
     * @return 1 if OPCode in { 0x1C; Ox3C; 0x5C, 0x7C, 0xDC, 0xFC} 0 otherwise
     */
    private int nop() {
        switch(opcode) {
            case 0x1C:
            case 0x3C:
            case 0x5C:
            case 0x7C:
            case 0xDC:
            case 0xFC:
                return 1;
        }
        return 0;
    }

    /**
     * Compute a Logic OR between the Accumulator and the Fetched data
     * stores it into the Accumulator
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 1 Extra cycle may be required
     */
    private int ora() {
        fetch();
        a =  (a | fetched) & 0x00FF;
        setFlag(Flags.Z, a == 0x00);
        setFlag(Flags.N, (a & 0x0080) == 0x0080);
        return 1;
    }

    /**
     * Push the Accumulator to the Stack
     * No Flag Update
     * @return 0 No extra cycle required
     */
    private int pha() {
        write(0x0100 + (stkp & 0x00FF), a & 0x00FF);
        stkp = (stkp-1) & 0x00FF;
        return 0;
    }

    /**
     * Push the Status Register to the Stack
     * B Flag set
     * U Flag set
     * @return 0 No extra cycle required
     */
    private int php() {
        write(0x0100 + (stkp & 0x00FF),  ((status | Flags.U.value | Flags.B.value) & 0x00FF));
        setFlag(Flags.B, false);
        setFlag(Flags.U, false);
        stkp = (stkp-1) & 0x00FF;
        return 0;
    }

    /**
     * Pool the Accumulator from the Stack
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 0 No extra cycle required
     */
    private int pla() {
        stkp = (stkp+1) & 0x00FF;
        a = read(0x0100 + stkp);
        setFlag(Flags.Z, a == 0x00);
        setFlag(Flags.N, (a & 0x80) != 0x00);
        return 0;
    }

    /**
     * Pool the Status Register from the Stack
     * U Flag set
     * All Flags set to the pulled Status Register value
     * @return 0 No extra cycle required
     */
    private int plp() {
        stkp = (stkp+1) & 0x00FF;
        status = read(0x0100 + stkp);
        setFlag(Flags.U, true);
        return 0;
    }

    /**
     * Rotate Left the Fetched data and store it to the Accumulator
     * The LSB is set using the Carry Flag
     * C Flag set if Fetched has MSB set
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 0 No extra cycle required
     */
    private int rol() {
        fetch();
        tmp =  ((getFlag(Flags.C) ? 1 : 0) | fetched << 1);
        setFlag(Flags.C, (tmp & 0xFF00) != 0x0000);
        setFlag(Flags.Z, (tmp & 0x00FF) == 0x00);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        if (opcodes.get(opcode).addr_mode.equals("IMP"))
            a =  (tmp & 0x00FF);
        else
            write(addr_abs,  (tmp & 0x00FF));
        return 0;
    }

    /**
     * Rotate Right the Fetched data and store it to the Accumulator
     * The MSB is set using the Carry Flag
     * C Flag set if Fetched has LSB set
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set (if C Flag was set)
     * @return 0 No extra cycle required
     */
    private int ror() {
        fetch();
        tmp =  ((getFlag(Flags.C) ? 1 << 7 : 0) | fetched >> 1);
        setFlag(Flags.C, (fetched & 0x01) == 0x01);
        setFlag(Flags.Z, (tmp & 0x00FF) == 0x00);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        if (opcodes.get(opcode).addr_mode.equals("IMP"))
            a =  (tmp & 0x00FF);
        else
            write(addr_abs,  (tmp & 0x00FF));
        return 0;
    }

    /**
     * Return from Interrupt Subroutine by pooling the Program Counter and Status Register from the Stack
     * B Flag unset
     * U Flag unset
     * All Flags set to the pulled Status Register value
     * @return 0 No extra cycle required
     */
    private int rti() {
        stkp = (stkp+1) & 0x00FF;
        status = read(0x0100 + stkp);
        status &= ~Flags.B.value;
        status &= ~Flags.U.value;

        stkp = (stkp+1) & 0x00FF;
        pc = read(0x0100 + stkp);
        stkp = (stkp+1) & 0x00FF;
        pc |= read(0x0100 + stkp) << 8;
        return 0;
    }

    /**
     * Return from Subroutine by pulling the Program Counter from the Stack
     * No Flag Update
     * @return 0 No extra cycle required
     */
    private int rts() {
        stkp = (stkp+1) & 0x00FF;
        pc = read(0x0100 + stkp);
        stkp = (stkp+1) & 0x00FF;
        pc |= read(0x0100 + stkp) << 8;
        pc = (pc+1) & 0xFFFF;
        return 0;
    }

    /**
     * Subtract the fetched value from the Accumulator
     * C Flag set if Accumulator < Fetched
     * Z Flag set if Accumulator = Fetched
     * N Flag set if the final Accumulator has MSB set
     * V Flag set the same way as ADC
     * @return 1 An extra cycle can be required depending on the addressing mode
     */
    private int sbc() {
        fetch();
        int value =  (fetched ^ 0x00FF);

        tmp =  (a + value + (getFlag(Flags.C) ? 0x1 : 0x0)) & 0x01FF;
        setFlag(Flags.C, tmp > 0x00FF);
        setFlag(Flags.Z, (tmp & 0x00FF) == 0);
        setFlag(Flags.N, (tmp & 0x0080) == 0x0080);
        setFlag(Flags.V, ((tmp ^ a) & (tmp ^ value) & 0x0080) == 0x0080);
        a =  (tmp & 0x00FF);
        return 1;
    }

    /**
     * Set the Carry Flag of the Status Register to 1
     * C Flag set
     * @return 0 No extra cycle required
     */
    private int sec() {
        setFlag(Flags.C, true);
        return 0;
    }

    /**
     * Set the Decimal Flag of the Status Register to 1
     * D Flag set
     * @return 0 No extra cycle required
     */
    private int sed() {
        setFlag(Flags.D, true);
        return 0;
    }

    /**
     * Set the Interrupt Flag of the Status Register to 1
     * I Flag set
     * @return 0 No extra cycle required
     */
    private int sei() {
        setFlag(Flags.I, true);
        return 0;
    }

    /**
     * Store the Accumulator to the fetched Address
     * No Flag Update
     * @return 0 No extra cycle required
     */
    private int sta() {
        write(addr_abs, a & 0x00FF);
        return 0;
    }

    /**
     * Store the X Register to the fetched Address
     * No Flag Update
     * @return 0 No extra cycle required
     */
    private int stx() {
        write(addr_abs, x & 0x00FF);
        return 0;
    }

    /**
     * Store the Y Register to the fetched Address
     * No Flag Update
     * @return 0 No extra cycle required
     */
    private int sty() {
        write(addr_abs, y & 0x00FF);
        return 0;
    }

    /**
     * Copy the Accumulator to the X Register
     * Z Flag set if X Register = 0
     * N Flag set if X Register has MSB set
     * @return 0 No extra cycle required
     */
    private int tax() {
        x = a & 0x00FF;
        setFlag(Flags.Z, x == 0x00);
        setFlag(Flags.N, (x & 0x80) != 0x00);
        return 0;
    }

    /**
     * Copy the Accumulator to the Y Register
     * Z Flag set if y Register = 0
     * N Flag set if Y Register has MSB set
     * @return 0 No extra cycle required
     */
    private int tay() {
        y = a  & 0x00FF;
        setFlag(Flags.Z, y == 0x00);
        setFlag(Flags.N, (y & 0x80) != 0x00);
        return 0;
    }

    /**
     * Copy the Stack Pointer to the X Register
     * Z Flag set if X Register = 0
     * N Flag set if X Register has MSB set
     * @return 0 No extra cycle required
     */
    private int tsx() {
        x = stkp & 0x00FF;
        setFlag(Flags.Z, x == 0x00);
        setFlag(Flags.N, (x & 0x80) != 0x00);
        return 0;
    }

    /**
     * Copy the X Register to the Accumulator
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 0 No extra cycle required
     */
    private int txa() {
        a = x & 0x00FF;
        setFlag(Flags.Z, a == 0x00);
        setFlag(Flags.N, (a & 0x80) != 0x00);
        return 0;
    }

    /**
     * Copy the X Register to the Stack Pointer
     * No Flag Update
     * @return 0 No extra cycle required
     */
    private int txs() {
        stkp = x & 0x00FF;
        return 0;
    }

    /**
     * Copy the Y Register to the Accumulator
     * Z Flag set if Accumulator = 0
     * N Flag set if Accumulator has MSB set
     * @return 0 No extra cycle required
     */
    private int tya() {
        a = y & 0x00FF;
        setFlag(Flags.Z, a == 0x00);
        setFlag(Flags.N, (a & 0x80) != 0x00);
        return 0;
    }

    /**
     * Every Illegal OPCodes
     * not implemented yet
     * @return always 0
     */
    private int xxx() {
        return 0;
    }




    // ========================================================= Utility Methods ========================================================= //

    /**
     * Execute one tick of the CPU
     */
    public void clock() {
        cpu_clock++;
        //If the CPU has finished the last Instruction
        if (cycles == 0) {
            //Fetch the Operation Code
            opcode = read(pc);
            //Increment the Program Counter
            pc = (pc+1) & 0xFFFF;
            //Get the Instruction
            Instruction instr = opcodes.get(opcode);
            //Set the required number of cycle for this instruction
            cycles = instr.cycles;
            //Execute the Instruction (Fetch data + treatment)
            int additional_cycle_1 = instr.addrmode();
            int additional_cycle_2 = instr.operate();
            //If the Instruction is susceptible of requiring an extra cycle and the addressing mode require one, the the Instruction require an extra cycle
            cycles += (additional_cycle_1 & additional_cycle_2);
        }
        //Decrement the remaining busy cycle index
        cycles--;
    }

    /**
     * Reset the CPU to the default state
     */
    public void reset() {
        a = 0x00;
        x = 0x00;
        y = 0x00;
        stkp = 0xFD;
        status = Flags.U.value;

        addr_abs = 0xFFFC;
        int low = read(addr_abs);
        int high = read(addr_abs + 1);
        pc = high << 8 | low;

        addr_rel = 0x0000;
        addr_abs = 0x0000;
        fetched = 0x00;

        cycles = 8;
    }

    /**
     * Trigger an Interrupt
     */
    public void irq() {
        //The Interrupt is trigger only if they aren't disable (I Flag of the Status Register)
        if (!getFlag(Flags.I)) {
            //Push the current Program Counter to the Stack LSB first
            write(0x0100 + stkp,  ((pc >> 8) & 0x00FF));
            stkp = (stkp-1) & 0x00FF;
            write(0x0100 + stkp,  (pc & 0x00FF));
            stkp = (stkp-1) & 0x00FF;
            //Push the current Status Register to the Stack
            setFlag(Flags.B, false);
            setFlag(Flags.U, true);
            setFlag(Flags.I, true);
            write(0x0100 + stkp, status);
            stkp = (stkp-1) & 0x00FF;
            //Jump to the NMI Routine specified at 0xFFFA
            addr_abs = 0xFFFE;
            int low = read(addr_abs);
            int high = read(addr_abs + 1);
            pc = high << 8 | low;
            // An Interrupt take 7 cycles
            cycles = 7;
        }
    }

    /**
     * Trigger a Non Maskable Interrupt
     */
    public void nmi() {
        //Push the current Program Counter to the Stack LSB first
        write(0x0100 + stkp,  ((pc >> 8) & 0x00FF));
        stkp = (stkp-1) & 0x00FF;
        write(0x0100 + stkp,  (pc & 0x00FF));
        stkp = (stkp-1) & 0x00FF;
        //Push the current Status Register to the Stack
        setFlag(Flags.B, false);
        setFlag(Flags.U, true);
        setFlag(Flags.I, true);
        write(0x0100 + stkp, status);
        stkp = (stkp-1) & 0x00FF;
        //Jump to the NMI Routine specified at 0xFFFA
        addr_abs = 0xFFFA;
        int low = read(addr_abs);
        int high = read(addr_abs + 1);
        pc = high << 8 | low;
        //An NMI take 8 cycles
        cycles = 8;
    }

    /**
     * Update the fetched data used according to the current Instruction addressing mode
     */
    private void fetch() {
        if (!opcodes.get(opcode).addr_mode.equals("IMP"))
            fetched = read(addr_abs) & 0x00FF;
    }




    // ========================================================== Debug Methods ========================================================== //

    /**
     * Disassemble an address range into readable 6502 assembly
     * @param start range start address
     * @param end range end address
     * @return a Map with addresses as Keys and Instructions as Values
     */
    public Map<Integer, String> disassemble(int start, int end) {
        int addr = start;
        int line_addr;
        int value, low, high;

        Map<Integer, String> code = new TreeMap<>();

        while (addr < end) {
            line_addr = addr;
            String line = String.format("$%04X: ", addr);
            int opcode = bus.cpuRead(addr, true);
            addr = (addr+1) & 0x1FFFF;
            Instruction instr = opcodes.get(opcode);
            line += instr.name + " ";
            switch (instr.addr_mode) {
                case "IMP":
                    line += "{IMP}";
                    break;
                case "IMM":
                    value = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("#$%02X {IMM}", value);
                    break;
                case "ZP0":
                    low = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("$%02X {ZP0}", low);
                    break;
                case "ZPX":
                    low = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("$%02X, X {ZPX}", low);
                    break;
                case "ZPY":
                    low = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("$%02X, Y {ZPY}", low);
                    break;
                case "IZX":
                    low = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("($%02X, X) {IZX}", low);
                    break;
                case "IZY":
                    low = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line +=  String.format("($%02X), Y {IZY}", low);
                    break;
                case "ABS":
                    low = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    high = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("$%04X {ABS}", (high << 8) | low);
                    break;
                case "ABX":
                    low = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    high = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("$%04X, X {ABX}", (high << 8) | low);
                    break;
                case "ABY":
                    low = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    high = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("$%04X, Y {ABY}", (high << 8) | low);
                    break;
                case "IND":
                    low = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    high = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("($%04X) {IND}", (high << 8) | low);
                    break;
                case "REL":
                    value = bus.cpuRead(addr, true);
                    addr = (addr+1) & 0x1FFFF;
                    line += String.format("$%02X ", value) + String.format("[$%04X] {IND}", addr + (byte)(value));
            }
            code.put(line_addr, line);
        }
        return code;
    }

    /**
     * Return whether or not the current instruction is complete
     * Thread safe
     * @return is the current instruction complete
     */
    public synchronized boolean complete() {
        return cycles == 0;
    }

    /**
     * Return the current Accumulator value as an 8bit unsigned value
     * Thread safe
     * @return the current Y Accumulator value as an 8bit unsigned value
     */
    public synchronized int threadSafeGetA() {
        return a;
    }

    /**
     * Return the current X Register value as an 8bit unsigned value
     * Thread safe
     * @return the current X Register value as an 8bit unsigned value
     */
    public synchronized int threadSafeGetX() {
        return x;
    }

    /**
     * Return the current Y Register value as an 8bit unsigned value
     * Thread safe
     * @return the current Y Register value as an 8bit unsigned value
     */
    public synchronized int threadSafeGetY() {
        return y;
    }

    /**
     * Return the current Stack Pointer as an 8bit unsigned value
     * Thread safe
     * @return the current Stack Pointer as an 8bit unsigned value
     */
    public synchronized int threadSafeGetStkp() {
        return stkp;
    }

    /**
     * Return the current state of a CPU Flag
     * Thread safe
     * @param flag the Flag to get the value of
     * @return a boolean representing the current value of the selected Flag
     */
    public synchronized boolean threadSafeGetState(Flags flag) {
        return (status & flag.value) == flag.value;
    }

    /**
     * Return the current Program Counter
     * Thread safe
     * @return the current Program Counter
     */
    public synchronized int threadSafeGetStatus() {
        return status;
    }

    /**
     * Return the current Program Counter as a 16bit unsigned value
     * Thread safe
     * @return the current Program Counter as a 16bit unsigned value
     */
    public synchronized int threadSafeGetPc() {
        return pc;
    }

    /**
     * Return the number of CPU cycles from system startup
     * Thread Safe
     * @return total number of CPU cycles
     */
    public synchronized long threadSafeGetCpuClock() {
        return cpu_clock;
    }
}

/**
 * This class represent an Instruction that can be fetched and executed by the CPU
 */
abstract class Instruction {

    String name;
    String addr_mode;
    int cycles;

    /**
     * Execute the instruction
     * @return 1 if the operation is susceptible of requiring an extra cycle 0 otherwise
     */
    public abstract int operate();

    /**
     * Fetch the appropriate data
     * @return 1 if the data gathering require an extra cycle 0 otherwise
     */
    public abstract int addrmode();

    Instruction(String name, String addr_mode, int cycles) {
        this.name = name;
        this.addr_mode = addr_mode;
        this.cycles = cycles;
    }
}