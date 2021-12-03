# This work used "compression_lib.py" by Tushar, and the link is
# https://github.com/t14916/NoC_Compression/blob/main/compression_lib.py

# 64bit base, 1 byte offset
def no_delta_compressor1(data_flits):
    base_flit = data_flits[0]
    delta_flits = []

    for flit in data_flits[1:]:
        offset = flit - base_flit
        if -128 <= offset <= 127:
            delta_flits.append(offset)
        else:
            return False, None, None

    return True, base_flit, delta_flits


# 64bit base, 2 bytes offset
def no_delta_compressor2(data_flits):
    base_flit = data_flits[0]
    delta_flits = []

    for flit in data_flits[1:]:
        offset = flit - base_flit
        if -32768 <= offset <= 32767:
            delta_flits.append(offset)
        else:
            return False, None, None

    return True, base_flit, delta_flits


# our own proposal 1
def adapted_no_delta_compressor(data_flits):
    base_flit = data_flits[0]
    offset_flits = []
    delta_flits = []
    mask = 0
    for flit in data_flits:
        offset = flit - base_flit
        if -128 <= offset <= 127:
            offset_flits.append(offset)
            mask += 1
        else:
            offset_flits.append(0)
            delta_flits.append(flit)
        mask = mask << 1
    return base_flit, offset_flits, delta_flits, mask


# our own proposal 2
def another_adapted_no_delta_compressor(data_flits):
    delta_flits = []
    data_max = max(data_flits)
    data_min = min(data_flits)
    if data_max - data_min > 256:
        return False, None, None

    base_flit = (data_max + data_min) // 2
    for flit in data_flits:
        offset = flit - base_flit
        delta_flits.append(offset)

    return True, base_flit, delta_flits



def flit_zip(data_flits):
    return None


# The header of the zen_compression is an one-hot mask
def zero_compressor(data_flits):
    delta_flits = []
    mask = 0
    for flit in data_flits:
        if flit == 0:
            mask += 1
        else:
            delta_flits.append(flit)
        mask = mask << 1
    return mask, delta_flits
