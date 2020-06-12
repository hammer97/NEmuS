package core.apu;

import core.apu.channels.NoiseChannel;
import core.apu.channels.PulseChannel;

/**
 * This class represent the APU of the NES
 * it handle everything sound related
 */
public class APU_2A03 {

    private static final double clockTime = .333333333 / 1789773.0;
    public static int[] length_table = {10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14, 12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30};
    private static double volume = 1;

    private PulseChannel pulse_1;
    private PulseChannel pulse_2;
    private NoiseChannel noise;

    private int clock_counter = 0;
    private double totalTime = 0.0;
    private int frame_counter = 0;
    private boolean b5StepMode = false;
    private int writeTo4017 = -1;

    public boolean frameIRQ = false;

    private boolean raw_audio = false;

    /**
     * Create a new instance of an APU
     */
    public APU_2A03() {
        pulse_1 = new PulseChannel();
        pulse_2 = new PulseChannel();
        noise = new NoiseChannel();
        noise.sequencer.sequence = 0xDBDB;
    }

    /**
     * Return the current volume
     *
     * @return the current APU volume
     */
    public static double getVolume() {
        return volume;
    }

    /**
     * Set the master volume of the APU
     *
     * @param volume the volume to set between 0 and 1
     */
    public static synchronized void setVolume(double volume) {
        APU_2A03.volume = volume;
    }

    /**
     * Return the current audio sample as a value between -1 and 1
     *
     * @return the current audio sample as a value between -1 and 1
     */
    public double getSample() {
        return 0.00752 * ((pulse_1.output + 0.5) * 15 + (pulse_2.output + 0.5) * 15) + 0.00494 * noise.output * 15;
    }

    /**
     * Called when the CPU try to write to the APU
     *
     * @param addr the address to write to
     */
    public void cpuWrite(int addr, int data) {
        addr &= 0xFFFF;
        data &= 0xFF;
        switch (addr) {
            case 0x4000:
                pulse_1.writeDutyCycle(data);
                break;
            case 0x4001:
                pulse_1.writeSweep(data);
                break;
            case 0x4002:
                pulse_1.writeTimerLow(data);
                break;
            case 0x4003:
                pulse_1.writeTimerHigh(data);
                pulse_1.writeLengthCounterLoad(data);
                break;
            case 0x4004:
                pulse_2.writeDutyCycle(data);
                break;
            case 0x4005:
                pulse_2.writeSweep(data);
                break;
            case 0x4006:
                pulse_2.writeTimerLow(data);
                break;
            case 0x4007:
                pulse_2.writeTimerHigh(data);
                pulse_2.writeLengthCounterLoad(data);
                break;
            case 0x400C:
                noise.updateEnvelope(data);
                break;
            case 0x400E:
                noise.updateReload(data);
                break;
            case 0x400F:
                pulse_1.envelope.started = true;
                pulse_2.envelope.started = true;
                noise.envelope.started = true;
                noise.lengthCounter.counter = length_table[(data & 0xF8) >> 3];
                break;
            case 0x4015:
                pulse_1.enabled = false;
                pulse_2.enabled = false;
                noise.enabled = false;
                if ((data & 0x1) == 0x1) pulse_1.enabled = true;
                else pulse_1.lengthCounter.counter = 0;

                if ((data & 0x2) == 0x2) pulse_2.enabled = true;
                else pulse_2.lengthCounter.counter = 0;

                if ((data & 0x8) == 0x8) noise.enabled = true;
                else noise.lengthCounter.counter = 0;

                break;
            case 0x4017:
                b5StepMode = (data & 0x80) == 0x80;
                if ((data & 0x40) == 0x40) frameIRQ = false;
                writeTo4017 = 4;
                break;
        }
    }

    /**
     * Called when the CPU try to read from the APU
     * Only address 0x4015 is relevant for the CPU
     *
     * @param addr the address to read from
     * @return the value represent the state of the counters of every implemented channels
     */
    public int cpuRead(int addr) {
        int data = 0x00;
        if (addr == 0x4015) {
            data |= (pulse_1.lengthCounter.counter > 0) ? 0x01 : 0x00;
            data |= (pulse_1.lengthCounter.counter > 0) ? 0x02 : 0x00;
            data |= (noise.lengthCounter.counter > 0) ? 0x08 : 0x00;
        }
        return data;
    }

    /**
     * A system clock of the APU, sampling can be deactivated for better performance
     * but there will be no sound
     * when sampling is disabled we only update what is susceptible to be read (the length counters)
     *
     * @param enable_sampling if sampling is enabled
     */
    public void clock(boolean enable_sampling) {
        boolean clockEnvelope = false;
        boolean clockLengthCounter = false;

        totalTime += clockTime;
        if (clock_counter % 6 == 0) {
            //A write to 0x4017 will cause the frame counter to be reset after 4 CPU cycles (2 APU cycles)
            if (writeTo4017 == 0) {
                frame_counter = 0;
                writeTo4017 = -1;
                if (b5StepMode) {
                    pulse_1.lengthCounter.clock(pulse_1.enabled, pulse_1.halted);
                    pulse_2.lengthCounter.clock(pulse_2.enabled, pulse_2.halted);
                    noise.lengthCounter.clock(noise.enabled, noise.halted);
                    if (enable_sampling) {
                        pulse_1.envelope.clock(pulse_1.halted);
                        pulse_2.envelope.clock(pulse_2.halted);
                        noise.envelope.clock(noise.halted);
                        pulse_1.sweeper.clock(pulse_1.sequencer.reload, false);
                        pulse_2.sweeper.clock(pulse_2.sequencer.reload, true);
                    }
                }
            }
            if (writeTo4017 >= 0)
                writeTo4017 -= 2;

            frame_counter++;
            if (b5StepMode) {
                if (frame_counter == 3729)
                    clockEnvelope = true;
                if (frame_counter == 7457) {
                    clockEnvelope = true;
                    clockLengthCounter = true;
                }
                if (frame_counter == 11186)
                    clockEnvelope = true;
                if (frame_counter == 18641) {
                    clockEnvelope = true;
                    clockLengthCounter = true;
                    frame_counter = 0;
                }
            } else {
                if (frame_counter == 3729)
                    clockEnvelope = true;
                if (frame_counter == 7457) {
                    clockEnvelope = true;
                    clockLengthCounter = true;
                }
                if (frame_counter == 11186)
                    clockEnvelope = true;
                if (frame_counter == 14916) {
                    clockEnvelope = true;
                    clockLengthCounter = true;
                    frame_counter = 0;
                    frameIRQ = true;
                }
            }
            if (clockEnvelope && enable_sampling) {
                pulse_1.envelope.clock(pulse_1.halted);
                pulse_2.envelope.clock(pulse_2.halted);
                noise.envelope.clock(noise.halted);
            }
            if (clockLengthCounter) {
                pulse_1.lengthCounter.clock(pulse_1.enabled, pulse_1.halted);
                pulse_2.lengthCounter.clock(pulse_2.enabled, pulse_2.halted);
                noise.lengthCounter.clock(noise.enabled, noise.halted);
                if (enable_sampling) {
                    pulse_1.sweeper.clock(pulse_1.sequencer.reload, false);
                    pulse_2.sweeper.clock(pulse_2.sequencer.reload, true);
                }
            }
            if (enable_sampling) {
                pulse_1.compute(totalTime, raw_audio);
                pulse_2.compute(totalTime, raw_audio);
                noise.compute();
            }
        }
        if (enable_sampling) {
            pulse_1.sweeper.track(pulse_1.sequencer.reload);
            pulse_2.sweeper.track(pulse_2.sequencer.reload);
        }
        clock_counter++;
    }

    /**
     * Enable or Disable RAW Audio mode
     *
     * @param raw should RAW Audio be triggered or not
     */
    public void enabledRawMode(boolean raw) {
        raw_audio = raw;
    }

    public boolean irq() {
        if (frameIRQ) {
            frameIRQ = false;
            return true;
        }
        return false;
    }
}
