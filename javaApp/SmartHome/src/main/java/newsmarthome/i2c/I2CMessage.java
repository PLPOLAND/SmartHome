package newsmarthome.i2c;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * I2CMessage
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class I2CMessage {
    // address of device to send
    private int address;
    // data to send
    @NonNull
    private byte[] data;
    // 0 - low, 1 - high
    private int priority = 0;
    // status of message
    private I2CMessageStatus status = I2CMessageStatus.WAITING;

    /**
     * I2CMessage constructor.
     * 
     * Low priority message
     * 
     * @param address - address of device to send
     * @param data - data to send
     * 
     */
    public I2CMessage(int address, byte[] data) {
        this.address = address;
        this.data = data;
        this.priority = 0;
    }

    public void setSent() {
        this.status = I2CMessageStatus.SENT;
    }

    public void setError() {
        this.status = I2CMessageStatus.ERROR;
    }

    public void setWaiting() {
        this.status = I2CMessageStatus.WAITING;
    }

    public boolean isWaiting() {
        return this.status == I2CMessageStatus.WAITING;
    }

    public boolean isSent() {
        return this.status == I2CMessageStatus.SENT;
    }

    public boolean isError() {
        return this.status == I2CMessageStatus.ERROR;
    }

    public boolean isHighPriority() {
        return this.priority == 1;
    }

    public boolean isLowPriority() {
        return this.priority == 0;
    }

    public int compareTo(I2CMessage message) {
        if (this.isHighPriority() && message.isLowPriority()) {
            return 1;
        } else if (this.isLowPriority() && message.isHighPriority()) {
            return -1;
        } else {
            return 0;
        }
    }

    public void waitToSend() {
        while (!this.isSent()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }
    }
    
}

enum I2CMessageStatus {
    WAITING,
    SENT,
    ERROR;


}