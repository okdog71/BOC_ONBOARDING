package com.grovesoft.boctracker.common.meta;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MetaService {

    public Map<String, List<String>> enums() {
        return CodeValues.all();
    }
}
