package com.test.demo.model;

import com.test.demo.util.MisfireInstructionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MisfireInstructionDto {
    @NotBlank(message = "{ignore.notblank}")
    private boolean ignore;
    private MisfireInstructionType instruction;
}
