/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package protopoet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.io.IOException;

/**
 * Defines a Service in the Protocol Buffer language.
 * Learn more: https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#service_definition
 */
public final class ServiceSpec implements Emittable, Buildable<ServiceSpec>, Useable.Name {

  /** Creates a builder for a {@link ServiceSpec}. */
  public static Builder builder(String serviceName) {
    checkNotNull(serviceName, "service name may not be null");
    return new Builder(serviceName);
  }

  private final String serviceName;
  private final ImmutableList<String> serviceComment;
  private final ImmutableList<RpcFieldSpec> rpcFields;
  private final ImmutableList<OptionSpec> options;
  private final UsedFieldMonitor usedFieldMonitor = new UsedFieldMonitor();

  private ServiceSpec(Builder builder) {
    serviceName = builder.serviceName;
    serviceComment = ImmutableList.copyOf(builder.serviceComment);
    rpcFields = ImmutableList.copyOf(builder.rpcFields);
    options = ImmutableList.copyOf(builder.options);
  }

  @Override
  public void emit(ProtoWriter writer) throws IOException {
    usedFieldMonitor.reset();
    if (!serviceComment.isEmpty()) {
      writer.emitComment(serviceComment);
    }
    writer.emit(String.format("service %s {", serviceName));

    // Options are hoisted to the very top of the service for clarity/consistency.
    if (!options.isEmpty()) {
      writer
        .emit("\n")
        .indent();
      for (OptionSpec option : options) {
        option.emit(writer);
      }
      writer.unindent();
    }

    if (!rpcFields.isEmpty()) {
      writer
        .emit("\n")
        .indent();
      for (RpcFieldSpec field : rpcFields) {
        try {
          usedFieldMonitor.add(field);
          field.emit(writer);
        } catch (UsageException ex) {
          throw new IOException(ex);
        }
      }
      writer.unindent();
    }
    writer.emit("}\n");
  }

  @Override
  public ServiceSpec build() {
    return this;
  }

  @Override
  public String name() {
    return serviceName;
  }

  /** Builder for producing new instances of {@link ServiceSpec}. */
  public static final class Builder implements Buildable<ServiceSpec> {

    private final String serviceName;
    private ImmutableList<String> serviceComment = ImmutableList.of();
    private ImmutableList<RpcFieldSpec> rpcFields = ImmutableList.of();
    private ImmutableList<OptionSpec> options = ImmutableList.of();

    private Builder(String serviceName) {
      this.serviceName = serviceName;
    }

    /** Declares a comment for the service. */
    public Builder setServiceComment(Iterable<String> lines) {
      this.serviceComment = ImmutableList.copyOf(lines);
      return this;
    }

    /** Declares a comment for the service. */
    public Builder setServiceComment(String... lines) {
      return setServiceComment(ImmutableList.copyOf(lines));
    }

    /** Adds an rpc field to the service, see {@link RpcFieldSpec}. */
    public Builder addRpcFields(Iterable<Buildable<RpcFieldSpec>> rpcFields) {
      this.rpcFields = ImmutableList.<RpcFieldSpec>builder()
        .addAll(this.rpcFields)
        .addAll(Buildables.buildAll(rpcFields))
        .build();
      return this;
    }

    /** Adds an rpc field to the service, see {@link RpcFieldSpec}. */
    @SafeVarargs
    public final Builder addRpcFields(Buildable<RpcFieldSpec>... rpcFields) {
      return addRpcFields(ImmutableList.copyOf(rpcFields));
    }

    /** Adds options to the service. See {@link OptionSpec}. */
    public Builder addServiceOptions(Iterable<Buildable<OptionSpec>> options) {
      this.options = ImmutableList.<OptionSpec>builder()
        .addAll(this.options)
        .addAll(Buildables.buildAll(options,
                                   opt -> checkArgument(opt.optionType() == OptionType.SERVICE,
                                                        "option must be service type")))
        .build();
      return this;
    }

    /** Adds options to the service. See {@link OptionSpec}. */
    public Builder addServiceOptions(Buildable<OptionSpec>... options) {
      return addServiceOptions(ImmutableList.copyOf(options));
    }

    /** Builds an instance of {@link ServiceSpec}. */
    @Override
    public ServiceSpec build() {
      return new ServiceSpec(this);
    }
  }
}
