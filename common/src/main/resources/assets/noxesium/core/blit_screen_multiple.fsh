#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;
uniform sampler2D Sampler4;
uniform sampler2D Sampler5;
uniform sampler2D Sampler6;
uniform sampler2D Sampler7;
uniform int SamplerCount;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 result = texture(Sampler0, texCoord);
    vec4 sample = result;

    if (SamplerCount >= 2) {
        sample = texture(Sampler1, texCoord);
        if (sample.a > 0) {
            result = sample + result * (1 - sample.a);
        }
    }
    if (SamplerCount >= 3) {
        sample = texture(Sampler2, texCoord);
        if (sample.a > 0) {
            result = sample + result * (1 - sample.a);
        }
    }
    if (SamplerCount >= 4) {
        sample = texture(Sampler3, texCoord);
        if (sample.a > 0) {
            result = sample + result * (1 - sample.a);
        }
    }
    if (SamplerCount >= 5) {
        sample = texture(Sampler4, texCoord);
        if (sample.a > 0) {
            result = sample + result * (1 - sample.a);
        }
    }
    if (SamplerCount >= 6) {
        sample = texture(Sampler5, texCoord);
        if (sample.a > 0) {
            result = sample + result * (1 - sample.a);
        }
    }
    if (SamplerCount >= 7) {
        sample = texture(Sampler6, texCoord);
        if (sample.a > 0) {
            result = sample + result * (1 - sample.a);
        }
    }
    if (SamplerCount >= 8) {
        sample = texture(Sampler7, texCoord);
        if (sample.a > 0) {
            result = sample + result * (1 - sample.a);
        }
    }

    fragColor = result;
}
