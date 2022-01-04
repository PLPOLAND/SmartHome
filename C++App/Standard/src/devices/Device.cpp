#include "Device.h"

    Device::Device()
    {
        this->setType(Device::TYPE::BRAK);
        this->id = 0;
    }

    Device::Device(TYPE type)
    {
        this->setType(type);
        this->id = 0;
    }
    Device::Device(TYPE type, byte id)
    {
        this->setType(type);
        this->id = id;
    }

    Device::Device(const Device &d){
        this->setType(d.type_of_device);
        this->id = d.id;
    }

    void Device::setType(TYPE type)
    {
        this->type_of_device = type;
    }

    Device::TYPE Device::getType()
    {
        return this->type_of_device;
    };

    void Device::setId(byte id)
    {
        this->id = id;
    };
    byte Device::getId()
    {
        return this->id;
    };
