# This work is based on Tushar's work https://github.com/t14916/NoC_Compression/blob/main/compression_lib.py
def no_delta(data_flits):
    mask = int("0xfffffffffffffffffff0")
    base_flit = data_flits[0]
    delta_flits = []

    for flit in data_flits[1:]:
        del_flit = base_flit & ~flit
        delta_flits.append(del_flit)
        if mask & del_flit:
            return None

    return delta_flits


def flit_zip(data_flits):
    return None


def zenco(data_flits):
    delta_flits = []
    mask = 0
    for flit in data_flits:
        if flit == 0:
            mask += 1
        else:
            delta_flits.append(flit)
        mask = mask << 1
    return mask, delta_flits