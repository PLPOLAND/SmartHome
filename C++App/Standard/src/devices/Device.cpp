#include "Device.h"

    Device::Device() : type_of_device(Device::TYPE::BRAK), id(0)
    {
    }

    Device::Device(TYPE type) : type_of_device(type), id(0)
    {
    }
    Device::Device(TYPE type, byte id) : type_of_device(type), id(id)
    {
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
