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
 * Defines a message in the Protocol Buffer language. Learn more:
 * https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#message_definition
 */
public final class MessageSpec implements Emittable, Buildable<MessageSpec>, Useable.Name {

  /** Creates a builder for a {@link MessageSpec} */
  public static Builder builder(String messageName) {
    checkNotNull(messageName, "message name may not be null");
    return new Builder(messageName);
  }

  private final String messageName;
  private final ImmutableList<String> messageComment;
  private final ImmutableList<ImmutableList<Emittable>> srcBlocks;
  private final ImmutableList<ReservationSpec> reservations;
  private final ImmutableList<OptionSpec> options;
  private final UsedFieldMonitor usedFieldMonitor = new UsedFieldMonitor();
  private final UsedNameMonitor usedNameMonitor;

  private MessageSpec(Builder builder) {
    messageName = builder.messageName;
    messageComment = ImmutableList.copyOf(builder.messageComment);
    reservations = ImmutableList.copyOf(builder.reservations);
    options = ImmutableList.copyOf(builder.options);
    srcBlocks = ImmutableList.copyOf(builder.srcBlocks);
    usedNameMonitor = new UsedNameMonitor(messageName);
  }

  @Override
  public void emit(ProtoWriter writer) throws IOException {
    usedFieldMonitor.reset();
    usedNameMonitor.reset();
    if (!messageComment.isEmpty()) {
      writer.emitComment(messageComment);
    }
    writer.emit(String.format("message %s {", messageName));

    // Options are hoisted to the very top of the message for clarity/consistency.
    if (!options.isEmpty()) {
      writer.emit("\n").indent();
      for (OptionSpec option : options) {
        option.emit(writer);
      }
      writer.unindent();
    }

    // Reservations are hoisted to the top as well, and added to the UsedFieldMonitor
    // first so we can track if the following src blocks accidentally use them.
    if (!reservations.isEmpty()) {
      writer.emit("\n").indent();
      for (ReservationSpec reservation : reservations) {
        try {
          usedFieldMonitor.add(reservation);
          reservation.emit(writer);
        } catch (UsageException ex) {
          throw new IOException(ex);
        }
      }
      writer.unindent();
    }

    // Callers could ask for any order of things to be written,
    // so we generically handle this via Emittable.
    for (ImmutableList<Emittable> srcBlock : srcBlocks) {
      writer.emit("\n");
      for (Emittable emittable : srcBlock) {
        try {
          // Capture the name of the emittable being written.
          if (emittable instanceof Useable.Name) {
            usedNameMonitor.add((Useable.Name) emittable);
          }
          // A single useable field.
          if (emittable instanceof Useable.Field) {
            usedFieldMonitor.add((Useable.Field) emittable);
          }
          // A container of usable fields, relevant to this current scope. eg: oneof.
          if (emittable instanceof Useable.Fields) {
            usedFieldMonitor.add((Useable.Fields) emittable);
          }
        } catch (UsageException ex) {
          throw new IOException(ex);
        }
        writer.indent();
        emittable.emit(writer);
        writer.unindent();
      }
    }
    writer.emit("}\n");
  }

  @Override
  public MessageSpec build() {
    return this;
  }

  @Override
  public String name() {
    return messageName;
  }

  /** Builder for a {@link MessageSpec}. */
  public static final class Builder implements Buildable<MessageSpec> {

    private final String messageName;
    private ImmutableList<String> messageComment = ImmutableList.of();
    private ImmutableList<ImmutableList<Emittable>> srcBlocks = ImmutableList.of();
    private ImmutableList<ReservationSpec> reservations = ImmutableList.of();
    private ImmutableList<OptionSpec> options = ImmutableList.of();

    private Builder(String messageName) {
      this.messageName = messageName;
    }

    /** Declares a comment for the message. */
    public Builder setMessageComment(Iterable<String> lines) {
      messageComment = ImmutableList.copyOf(lines);
      return this;
    }

    /** Declares a comment for the message. */
    public Builder setMessageComment(String... lines) {
      return setMessageComment(ImmutableList.copyOf(lines));
    }

    /** Adds enums to the inside of the message, see {@link EnumSpec}. */
    public Builder addEnums(Iterable<? extends Buildable<EnumSpec>> enums) {
      srcBlocks =
          ImmutableList.<ImmutableList<Emittable>>builder()
              .addAll(srcBlocks)
              .add(ImmutableList.copyOf(Buildables.buildAll(enums)))
              .build();
      return this;
    }

    /** Adds enums to the inside of the message, see {@link EnumSpec}. */
    @SafeVarargs
    public final Builder addEnums(Buildable<EnumSpec>... enums) {
      return addEnums(ImmutableList.copyOf(enums));
    }

    /** Adds field reservations for the message, see {@link ReservationSpec}. */
    public Builder addReservations(Iterable<? extends Buildable<ReservationSpec>> resos) {
      reservations =
          ImmutableList.<ReservationSpec>builder()
              .addAll(reservations)
              .addAll(Buildables.buildAll(resos))
              .build();
      return this;
    }

    /** Adds field reservations for the message, see {@link ReservationSpec}. */
    public Builder addReservations(Buildable<ReservationSpec>... resos) {
      return addReservations(ImmutableList.copyOf(resos));
    }

    /** Adds inner messages to a message. */
    public Builder addMessages(Iterable<? extends Buildable<MessageSpec>> msgs) {
      srcBlocks =
          ImmutableList.<ImmutableList<Emittable>>builder()
              .addAll(srcBlocks)
              .add(ImmutableList.copyOf(Buildables.buildAll(msgs)))
              .build();
      return this;
    }

    /** Adds inner messages to a message. */
    public Builder addMessages(Buildable<MessageSpec>... msgs) {
      return addMessages(ImmutableList.copyOf(msgs));
    }

    /** Adds message fields to the message. see {@link MessageFieldSpec}. */
    public Builder addMessageFields(Iterable<? extends Buildable<MessageField>> fields) {
      srcBlocks =
          ImmutableList.<ImmutableList<Emittable>>builder()
              .addAll(srcBlocks)
              .add(ImmutableList.copyOf(Buildables.buildAll(fields)))
              .build();
      return this;
    }

    /**
     * Adds message field to the message. see {@link MessageFieldSpec}, {@link OneofFieldSpec}, and
     * {@link MapFieldSpec}.
     */
    public Builder addMessageFields(Buildable<MessageField>... fields) {
      return addMessageFields(ImmutableList.copyOf(fields));
    }

    /** Adds options to the message. See {@link OptionSpec}. */
    public Builder addMessageOptions(Iterable<? extends Buildable<OptionSpec>> options) {
      this.options =
          ImmutableList.<OptionSpec>builder()
              .addAll(this.options)
              .addAll(
                  Buildables.buildAll(
                      options,
                      opt ->
                          checkArgument(
                              opt.optionType() == OptionType.MESSAGE,
                              "option must be message type")))
              .build();
      return this;
    }

    /** Adds options to the message. See {@link OptionSpec}. */
    public Builder addMessageOptions(Buildable<OptionSpec>... options) {
      return addMessageOptions(ImmutableList.copyOf(options));
    }

    /** Builds a new instance of {@link MessageSpec}. */
    @Override
    public MessageSpec build() {
      return new MessageSpec(this);
    }
  }
}
